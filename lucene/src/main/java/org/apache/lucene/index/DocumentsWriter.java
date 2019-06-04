/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.index;


import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DocumentsWriterPerThread.FlushedSegment;
import org.apache.lucene.index.DocumentsWriterPerThreadPool.ThreadState;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.InfoStream;

/**
 * This class accepts multiple added documents and directly
 * writes segment files.
 *
 * Each added document is passed to the indexing chain,
 * which in turn processes the document into the different
 * codec formats.  Some formats write bytes to files
 * immediately, e.g. stored fields and term vectors, while
 * others are buffered by the indexing chain and written
 * only on flush.
 *
 * Once we have used our allowed RAM buffer, or the number
 * of added docs is large enough (in the case we are
 * flushing by doc count instead of RAM usage), we create a
 * real segment and flush it to the Directory.
 *
 * Threads:
 *
 * Multiple threads are allowed into addDocument at once.
 * There is an initial synchronized call to getThreadState
 * which allocates a ThreadState for this thread.  The same
 * thread will get the same ThreadState over time (thread
 * affinity) so that if there are consistent patterns (for
 * example each thread is indexing a different content
 * source) then we make better use of RAM.  Then
 * processDocument is called on that ThreadState without
 * synchronization (most of the "heavy lifting" is in this
 * call).  Finally the synchronized "finishDocument" is
 * called to flush changes to the directory.
 *
 * When flush is called by IndexWriter we forcefully idle
 * all threads and flush only once they are all idle.  This
 * means you can call flush with a given thread even while
 * other threads are actively adding/deleting documents.
 *
 *
 * Exceptions:
 *
 * Because this class directly updates in-memory posting
 * lists, and flushes stored fields and term vectors
 * directly to files in the directory, there are certain
 * limited times when an exception can corrupt this state.
 * For example, a disk full while flushing stored fields
 * leaves this file in a corrupt state.  Or, an OOM
 * exception while appending to the in-memory posting lists
 * can corrupt that posting list.  We call such exceptions
 * "aborting exceptions".  In these cases we must call
 * abort() to discard all docs added since the last flush.
 *
 * All other exceptions ("non-aborting exceptions") can
 * still partially update the index structures.  These
 * updates are consistent, but, they represent only a part
 * of the document seen up until the exception was hit.
 * When this happens, we immediately mark the document as
 * deleted so that the document is always atomically ("all
 * or none") added to the index.
 */

final class DocumentsWriter implements Closeable, Accountable {
  private final Directory directoryOrig; // no wrapping, for infos
  private final Directory directory;
  private final FieldInfos.FieldNumbers globalFieldNumberMap;
  private final int indexCreatedVersionMajor;
  private final AtomicLong pendingNumDocs;
  private final boolean enableTestPoints;
  private final Supplier<String> segmentNameSupplier;
  private final FlushNotifications flushNotifications;

  private volatile boolean closed;

  private final InfoStream infoStream;

  private final LiveIndexWriterConfig config;

  private final AtomicInteger numDocsInRAM = new AtomicInteger(0);

  // TODO: cut over to BytesRefHash in BufferedDeletes
  volatile DocumentsWriterDeleteQueue deleteQueue;
  private final DocumentsWriterFlushQueue ticketQueue = new DocumentsWriterFlushQueue();
  /*
   * we preserve changes during a full flush since IW might not checkout before
   * we release all changes. NRT Readers otherwise suddenly return true from
   * isCurrent while there are actually changes currently committed. See also
   * #anyChanges() & #flushAllThreads
   */
  private volatile boolean pendingChangesInCurrentFullFlush;

  final DocumentsWriterPerThreadPool perThreadPool;
  final FlushPolicy flushPolicy;
  final DocumentsWriterFlushControl flushControl;
  private long lastSeqNo;
  
  DocumentsWriter(FlushNotifications flushNotifications, int indexCreatedVersionMajor, AtomicLong pendingNumDocs, boolean enableTestPoints,
                  Supplier<String> segmentNameSupplier, LiveIndexWriterConfig config, Directory directoryOrig, Directory directory,
                  FieldInfos.FieldNumbers globalFieldNumberMap) {
    this.indexCreatedVersionMajor = indexCreatedVersionMajor;
    this.directoryOrig = directoryOrig;
    this.directory = directory;
    this.config = config;
    this.infoStream = config.getInfoStream();
    this.deleteQueue = new DocumentsWriterDeleteQueue(infoStream);
    this.perThreadPool = config.getIndexerThreadPool();
    flushPolicy = config.getFlushPolicy();
    this.globalFieldNumberMap = globalFieldNumberMap;
    this.pendingNumDocs = pendingNumDocs;
    flushControl = new DocumentsWriterFlushControl(this, config);
    this.segmentNameSupplier = segmentNameSupplier;
    this.enableTestPoints = enableTestPoints;
    this.flushNotifications = flushNotifications;
  }
  
  long deleteQueries(final Query... queries) throws IOException {
    return applyDeleteOrUpdate(q -> q.addDelete(queries));
  }

  void setLastSeqNo(long seqNo) {
    lastSeqNo = seqNo;
  }

  long deleteTerms(final Term... terms) throws IOException {
    return applyDeleteOrUpdate(q -> q.addDelete(terms));
  }

  long updateDocValues(DocValuesUpdate... updates) throws IOException {
    return applyDeleteOrUpdate(q -> q.addDocValuesUpdates(updates));
  }

  private synchronized long applyDeleteOrUpdate(ToLongFunction<DocumentsWriterDeleteQueue> function) throws IOException {
    // TODO why is this synchronized?
    final DocumentsWriterDeleteQueue deleteQueue = this.deleteQueue;
    long seqNo = function.applyAsLong(deleteQueue);
    flushControl.doOnDelete();
    lastSeqNo = Math.max(lastSeqNo, seqNo);
    if (applyAllDeletes(deleteQueue)) {
      seqNo = -seqNo;
    }
    return seqNo;
  }
  
  /** If buffered deletes are using too much heap, resolve them and write disk and return true. */
  /* 如果缓冲删除使用太多堆，解析它们并写入磁盘并返回true。 */
  private boolean applyAllDeletes(DocumentsWriterDeleteQueue deleteQueue) throws IOException {
    if (flushControl.isFullFlush() == false // never apply deletes during full flush this breaks happens before relationship
        && flushControl.getAndResetApplyAllDeletes()) {
      if (deleteQueue != null) {
        assert assertTicketQueueModification(deleteQueue);
        ticketQueue.addDeletes(deleteQueue);
      }
      flushNotifications.onDeletesApplied(); // apply deletes event forces a purge
      return true;
    }
    return false;
  }

  void purgeFlushTickets(boolean forced, IOUtils.IOConsumer<DocumentsWriterFlushQueue.FlushTicket> consumer)
      throws IOException {
    if (forced) {
      ticketQueue.forcePurge(consumer);
    } else {
      ticketQueue.tryPurge(consumer);
    }
  }

  /** Returns how many docs are currently buffered in RAM. */
  int getNumDocs() {
    return numDocsInRAM.get();
  }

  private void ensureOpen() throws AlreadyClosedException {
    if (closed) {
      throw new AlreadyClosedException("this DocumentsWriter is closed");
    }
  }

  /** Called if we hit an exception at a bad time (when
   *  updating the index files) and must discard all
   *  currently buffered docs.  This resets our state,
   *  discarding any docs added since last flush. */
  synchronized void abort() throws IOException {
    boolean success = false;
    try {
      deleteQueue.clear();
      if (infoStream.isEnabled("DW")) {
        infoStream.message("DW", "abort");
      }
      final int limit = perThreadPool.getActiveThreadStateCount();
      for (int i = 0; i < limit; i++) {
        final ThreadState perThread = perThreadPool.getThreadState(i);
        perThread.lock();
        try {
          abortThreadState(perThread);
        } finally {
          perThread.unlock();
        }
      }
      flushControl.abortPendingFlushes();
      flushControl.waitForFlush();
      success = true;
    } finally {
      if (infoStream.isEnabled("DW")) {
        infoStream.message("DW", "done abort success=" + success);
      }
    }
  }

  final boolean flushOneDWPT() throws IOException {
    if (infoStream.isEnabled("DW")) {
      infoStream.message("DW", "startFlushOneDWPT");
    }
    // first check if there is one pending
    DocumentsWriterPerThread documentsWriterPerThread = flushControl.nextPendingFlush();
    if (documentsWriterPerThread == null) {
      documentsWriterPerThread = flushControl.checkoutLargestNonPendingWriter();
    }
    if (documentsWriterPerThread != null) {
      return doFlush(documentsWriterPerThread);
    }
    return false; // we didn't flush anything here
  }

  /** Locks all currently active DWPT and aborts them.
   *  The returned Closeable should be closed once the locks for the aborted
   *  DWPTs can be released. */
  synchronized Closeable lockAndAbortAll() throws IOException {
    if (infoStream.isEnabled("DW")) {
      infoStream.message("DW", "lockAndAbortAll");
    }
    // Make sure we move all pending tickets into the flush queue:
    ticketQueue.forcePurge(ticket -> {
      if (ticket.getFlushedSegment() != null) {
        pendingNumDocs.addAndGet(-ticket.getFlushedSegment().segmentInfo.info.maxDoc());
      }
    });
    List<ThreadState> threadStates = new ArrayList<>();
    AtomicBoolean released = new AtomicBoolean(false);
    final Closeable release = () -> {
      if (released.compareAndSet(false, true)) { // only once
        if (infoStream.isEnabled("DW")) {
          infoStream.message("DW", "unlockAllAbortedThread");
        }
        perThreadPool.clearAbort();
        for (ThreadState state : threadStates) {
          state.unlock();
        }
      }
    };
    try {
      deleteQueue.clear();
      final int limit = perThreadPool.getMaxThreadStates();
      perThreadPool.setAbort();
      for (int i = 0; i < limit; i++) {
        final ThreadState perThread = perThreadPool.getThreadState(i);
        perThread.lock();
        threadStates.add(perThread);
        abortThreadState(perThread);
      }
      deleteQueue.clear();

      // jump over any possible in flight ops:
      deleteQueue.skipSequenceNumbers(perThreadPool.getActiveThreadStateCount() + 1);

      flushControl.abortPendingFlushes();
      flushControl.waitForFlush();
      if (infoStream.isEnabled("DW")) {
        infoStream.message("DW", "finished lockAndAbortAll success=true");
      }
      return release;
    } catch (Throwable t) {
      if (infoStream.isEnabled("DW")) {
        infoStream.message("DW", "finished lockAndAbortAll success=false");
      }
      try {
        // if something happens here we unlock all states again
        release.close();
      } catch (Throwable t1) {
        t.addSuppressed(t1);
      }
      throw t;
    }
  }
  
  /** Returns how many documents were aborted. */
  private int abortThreadState(final ThreadState perThread) throws IOException {
    assert perThread.isHeldByCurrentThread();
    if (perThread.isInitialized()) { 
      try {
        int abortedDocCount = perThread.dwpt.getNumDocsInRAM();
        subtractFlushedNumDocs(abortedDocCount);
        perThread.dwpt.abort();
        return abortedDocCount;
      } finally {
        flushControl.doOnAbort(perThread);
      }
    } else {
      flushControl.doOnAbort(perThread);
      // This DWPT was never initialized so it has no indexed documents:
      return 0;
    }
  }

  /** returns the maximum sequence number for all previously completed operations */
  public long getMaxCompletedSequenceNumber() {
    long value = lastSeqNo;
    int limit = perThreadPool.getMaxThreadStates();
    for(int i = 0; i < limit; i++) {
      ThreadState perThread = perThreadPool.getThreadState(i);
      value = Math.max(value, perThread.lastSeqNo);
    }
    return value;
  }

  boolean anyChanges() {
    /*
     * changes are either in a DWPT or in the deleteQueue.
     * yet if we currently flush deletes and / or dwpt there
     * could be a window where all changes are in the ticket queue
     * before they are published to the IW. ie we need to check if the 
     * ticket queue has any tickets.
     */
    boolean anyChanges = numDocsInRAM.get() != 0 || anyDeletions() || ticketQueue.hasTickets() || pendingChangesInCurrentFullFlush;
    if (infoStream.isEnabled("DW") && anyChanges) {
      infoStream.message("DW", "anyChanges? numDocsInRam=" + numDocsInRAM.get()
                         + " deletes=" + anyDeletions() + " hasTickets:"
                         + ticketQueue.hasTickets() + " pendingChangesInFullFlush: "
                         + pendingChangesInCurrentFullFlush);
    }
    return anyChanges;
  }
  
  public int getBufferedDeleteTermsSize() {
    return deleteQueue.getBufferedUpdatesTermsSize();
  }

  //for testing
  public int getNumBufferedDeleteTerms() {
    return deleteQueue.numGlobalTermDeletes();
  }

  public boolean anyDeletions() {
    return deleteQueue.anyChanges();
  }

  @Override
  public void close() {
    closed = true;
    flushControl.setClosed();
  }

  private boolean preUpdate() throws IOException {
    ensureOpen();// 确认write打开
    boolean hasEvents = false;// 默认无事件

    // 如果flushControl 刷新控制器 认为需要刷新segement到持久化文件中
    if (flushControl.anyStalledThreads() || (flushControl.numQueuedFlushes() > 0 && config.checkPendingFlushOnUpdate)) {
      // Help out flushing any queued DWPTs so we can un-stall:
      do {
        //如果可能的话，在这里尝试获取挂起的线程
        DocumentsWriterPerThread flushingDWPT;
        while ((flushingDWPT = flushControl.nextPendingFlush()) != null) {
          // 不要在这里按下删除键，因为更新可能会失败!
          hasEvents |= doFlush(flushingDWPT);//执行flush刷写操作
        }
        
        flushControl.waitIfStalled(); // block if stalled
      } while (flushControl.numQueuedFlushes() != 0); // still queued DWPTs try help flushing
    }
    return hasEvents;
  }

  private boolean postUpdate(DocumentsWriterPerThread flushingDWPT, boolean hasEvents) throws IOException {
    // 如果删除占用太多内存，刷写到磁盘，返回true，如果hasEvent = hasEvenet | applyAllDeletes(deleteQueue) 只要有刷写持久化就是true
    hasEvents |= applyAllDeletes(deleteQueue);
    if (flushingDWPT != null) {// 如果当前flushingDWPT 不为null
      hasEvents |= doFlush(flushingDWPT);// 判断是否需要刷写磁盘
    } else if (config.checkPendingFlushOnUpdate) {
      // 如果fluashingDWPT 为null，又需要刷写 则根据刷新控制器 推出 下一个需要刷写的dwpt
      final DocumentsWriterPerThread nextPendingFlush = flushControl.nextPendingFlush();
      if (nextPendingFlush != null) {// 如果不为null
        hasEvents |= doFlush(nextPendingFlush);// 执行刷写操作
      }
    }
    // 返回
    return hasEvents;
  }
  
  private void ensureInitialized(ThreadState state) throws IOException {
    // 如果dwpt为null，刚初始化的state.dwpt就是null
    if (state.dwpt == null) {
      // 创建一个失败信息
      final FieldInfos.Builder infos = new FieldInfos.Builder(globalFieldNumberMap);
      // 创建dwpt
      state.dwpt = new DocumentsWriterPerThread(indexCreatedVersionMajor, segmentNameSupplier.get(), directoryOrig,
                                                directory, config, infoStream, deleteQueue, infos,
                                                pendingNumDocs, enableTestPoints);
    }
  }

  long updateDocuments(final Iterable<? extends Iterable<? extends IndexableField>> docs, final Analyzer analyzer,
                       final DocumentsWriterDeleteQueue.Node<?> delNode) throws IOException {
    boolean hasEvents = preUpdate();

    final ThreadState perThread = flushControl.obtainAndLock();
    final DocumentsWriterPerThread flushingDWPT;
    long seqNo;

    try {
      // This must happen after we've pulled the ThreadState because IW.close
      // waits for all ThreadStates to be released:
      ensureOpen();
      ensureInitialized(perThread);
      assert perThread.isInitialized();
      final DocumentsWriterPerThread dwpt = perThread.dwpt;
      final int dwptNumDocs = dwpt.getNumDocsInRAM();
      try {
        seqNo = dwpt.updateDocuments(docs, analyzer, delNode, flushNotifications);
      } finally {
        if (dwpt.isAborted()) {
          flushControl.doOnAbort(perThread);
        }
        // We don't know how many documents were actually
        // counted as indexed, so we must subtract here to
        // accumulate our separate counter:
        numDocsInRAM.addAndGet(dwpt.getNumDocsInRAM() - dwptNumDocs);
      }
      final boolean isUpdate = delNode != null && delNode.isDelete();
      // 给flushingDWOPT赋值 判断是否需要合并刷写到磁盘，需要的话，返回DWPT
      flushingDWPT = flushControl.doAfterDocument(perThread, isUpdate);

      assert seqNo > perThread.lastSeqNo: "seqNo=" + seqNo + " lastSeqNo=" + perThread.lastSeqNo;
      perThread.lastSeqNo = seqNo;

    } finally {
      perThreadPool.release(perThread);
    }

    if (postUpdate(flushingDWPT, hasEvents)) {
      seqNo = -seqNo;
    }
    return seqNo;
  }

  /**
   *
   * @param doc 要index的文档
   * @param analyzer  分词器
   * @param delNode   需要删除的节点
   * @return
   * @throws IOException
   */
  long updateDocument(final Iterable<? extends IndexableField> doc, final Analyzer analyzer,
                      final DocumentsWriterDeleteQueue.Node<?> delNode) throws IOException {

    boolean hasEvents = preUpdate();// 判断是否有预更新事件

    // 刷新控制器 获得线程并且锁定
    final ThreadState perThread = flushControl.obtainAndLock();

    // 刷新线程DWPT
    final DocumentsWriterPerThread flushingDWPT;
    long seqNo;
    try {
      // This must happen after we've pulled the ThreadState because IW.close
      // waits for all ThreadStates to be released:
      ensureOpen();// 确认打开
      // 确认初始化了，如果没有初始化，则初始化
      ensureInitialized(perThread);
      assert perThread.isInitialized();
      // dwpt赋值
      final DocumentsWriterPerThread dwpt = perThread.dwpt;
      // 这一个dwpt驻留在内存中的数量
      final int dwptNumDocs = dwpt.getNumDocsInRAM();
      try {
        // dwpt更新文档操作 flush Notifications
        seqNo = dwpt.updateDocument(doc, analyzer, delNode, flushNotifications);
      } finally {
        // 如果dwpt被意外中止
        if (dwpt.isAborted()) {
          // 意外中止的后续处理
          flushControl.doOnAbort(perThread);
        }
        // We don't know whether the document actually
        // counted as being indexed, so we must subtract here to
        // accumulate our separate counter:
        // 给numDocsInRAM重新赋值，增加上新增加的doc数量 (当前doc-之前的doc = 新增的doc数量)
        numDocsInRAM.addAndGet(dwpt.getNumDocsInRAM() - dwptNumDocs);
      }
      // 如果delNode存在，并且都删除了，则该操作isUpdate
      final boolean isUpdate = delNode != null && delNode.isDelete();
      // 更新，插入的后续操作
      flushingDWPT = flushControl.doAfterDocument(perThread, isUpdate);

      assert seqNo > perThread.lastSeqNo: "seqNo=" + seqNo + " lastSeqNo=" + perThread.lastSeqNo;
      // 最后一次操作的seqNo = 这次操作的seqNo
      perThread.lastSeqNo = seqNo;

    } finally {
      // 操作结束 释放当前线程的占用
      perThreadPool.release(perThread);
    }
    // 判断是否要把缓存数据刷写到磁盘
    if (postUpdate(flushingDWPT, hasEvents)) {
      seqNo = -seqNo;// 序号取负
    }
    
    return seqNo;
  }

  private boolean doFlush(DocumentsWriterPerThread flushingDWPT) throws IOException {
    boolean hasEvents = false;// 是否刷写 = false
    while (flushingDWPT != null) {// 刷写线程 != null
      hasEvents = true; // 是否刷写 = true
      boolean success = false;// 成功 = false
      DocumentsWriterFlushQueue.FlushTicket ticket = null;
      try {
        // 当前full flush del 队列为null || DWPT的删除队列 = full flush del 队列
        assert currentFullFlushDelQueue == null
            || flushingDWPT.deleteQueue == currentFullFlushDelQueue : "expected: "
            + currentFullFlushDelQueue + "but was: " + flushingDWPT.deleteQueue
            + " " + flushControl.isFullFlush();
        /*
         * Since with DWPT the flush process is concurrent and several DWPT
         * could flush at the same time we must maintain the order of the
         * flushes before we can apply the flushed segment and the frozen global
         * deletes it is buffering. The reason for this is that the global
         * deletes mark a certain point in time where we took a DWPT out of
         * rotation and freeze the global deletes.
         * 
         * Example: A flush 'A' starts and freezes the global deletes, then
         * flush 'B' starts and freezes all deletes occurred since 'A' has
         * started. if 'B' finishes before 'A' we need to wait until 'A' is done
         * otherwise the deletes frozen by 'B' are not applied to 'A' and we
         * might miss to deletes documents in 'A'.
         */
        /**
         * 因为使用DWPT，刷新过程是并发的，并且有多个DWPT，
         * 能否同时刷新我们必须保持的顺序在应用刷新段和冻结全局段之前刷新
         * 删除它正在缓冲。原因是全局删除标记出我们取出DWPT的时间点
         * 旋转和冻结全局删除。
         *
         * 示例:然后，刷新'A'启动并冻结全局删除
         * 刷新“B”并冻结自“A”之后发生的所有删除
         * 开始了。如果'B'在'A'之前完成，我们需要等到'A'完成
         * 否则，“B”冻结的删除不应用于“A”，
         * 我们可能会遗漏删除“A”中的文档。
         */
        try {
          assert assertTicketQueueModification(flushingDWPT.deleteQueue);
          // Each flush is assigned a ticket in the order they acquire the ticketQueue lock
          ticket = ticketQueue.addFlushTicket(flushingDWPT);
          final int flushingDocsInRam = flushingDWPT.getNumDocsInRAM();
          boolean dwptSuccess = false;
          try {
            // 同时刷新而不锁定 flush产生一个新的segement
            final FlushedSegment newSegment = flushingDWPT.flush(flushNotifications);
            ticketQueue.addSegment(ticket, newSegment);
            dwptSuccess = true;
          } finally {
            subtractFlushedNumDocs(flushingDocsInRam);
            if (flushingDWPT.pendingFilesToDelete().isEmpty() == false) {
              Set<String> files = flushingDWPT.pendingFilesToDelete();
              flushNotifications.deleteUnusedFiles(files);
              hasEvents = true;
            }
            if (dwptSuccess == false) {
              flushNotifications.flushFailed(flushingDWPT.getSegmentInfo());
              hasEvents = true;
            }
          }
          // flush was successful once we reached this point - new seg. has been assigned to the ticket!
          success = true;
        } finally {
          // 如果失败了
          if (!success && ticket != null) {
            // In the case of a failure make sure we are making progress and
            // apply all the deletes since the segment flush failed since the flush
            // ticket could hold global deletes see FlushTicket#canPublish()
            // 标记失败
            ticketQueue.markTicketFailed(ticket);
          }
        }
        /*
         * Now we are done and try to flush the ticket queue if the head of the
         * queue has already finished the flush.
         */
        if (ticketQueue.getTicketCount() >= perThreadPool.getActiveThreadStateCount()) {
          // This means there is a backlog: the one
          // thread in innerPurge can't keep up with all
          // other threads flushing segments.  In this case
          // we forcefully stall the producers.
          flushNotifications.onTicketBacklog();
          break;
        }
      } finally {
        flushControl.doAfterFlush(flushingDWPT);
      }
     
      flushingDWPT = flushControl.nextPendingFlush();
    }

    if (hasEvents) {
      flushNotifications.afterSegmentsFlushed();
    }

    // If deletes alone are consuming > 1/2 our RAM
    // buffer, force them all to apply now. This is to
    // prevent too-frequent flushing of a long tail of
    // tiny segments:
    final double ramBufferSizeMB = config.getRAMBufferSizeMB();
    if (ramBufferSizeMB != IndexWriterConfig.DISABLE_AUTO_FLUSH &&
        flushControl.getDeleteBytesUsed() > (1024*1024*ramBufferSizeMB/2)) {
      hasEvents = true;
      if (applyAllDeletes(deleteQueue) == false) {
        if (infoStream.isEnabled("DW")) {
          infoStream.message("DW", String.format(Locale.ROOT, "force apply deletes after flush bytesUsed=%.1f MB vs ramBuffer=%.1f MB",
                                                 flushControl.getDeleteBytesUsed()/(1024.*1024.),
                                                 ramBufferSizeMB));
        }
        flushNotifications.onDeletesApplied();
      }
    }

    return hasEvents;
  }

  interface FlushNotifications { // TODO maybe we find a better name for this?

    /**
     * Called when files were written to disk that are not used anymore. It's the implementation's responsibility
     * to clean these files up
     */
    void deleteUnusedFiles(Collection<String> files);

    /**
     * Called when a segment failed to flush.
     */
    void flushFailed(SegmentInfo info);

    /**
     * Called after one or more segments were flushed to disk.
     */
    void afterSegmentsFlushed() throws IOException;

    /**
     * Should be called if a flush or an indexing operation caused a tragic / unrecoverable event.
     */
    void onTragicEvent(Throwable event, String message);

    /**
     * Called once deletes have been applied either after a flush or on a deletes call
     */
    void onDeletesApplied();

    /**
     * Called once the DocumentsWriter ticket queue has a backlog. This means there is an inner thread
     * that tries to publish flushed segments but can't keep up with the other threads flushing new segments.
     * This likely requires other thread to forcefully purge the buffer to help publishing. This
     * can't be done in-place since we might hold index writer locks when this is called. The caller must ensure
     * that the purge happens without an index writer lock being held.
     *
     * @see DocumentsWriter#purgeFlushTickets(boolean, IOUtils.IOConsumer)
     */
    void onTicketBacklog();
  }
  
  void subtractFlushedNumDocs(int numFlushed) {
    int oldValue = numDocsInRAM.get();
    while (numDocsInRAM.compareAndSet(oldValue, oldValue - numFlushed) == false) {
      oldValue = numDocsInRAM.get();
    }
    assert numDocsInRAM.get() >= 0;
  }
  
  // for asserts
  private volatile DocumentsWriterDeleteQueue currentFullFlushDelQueue = null;

  // for asserts
  private synchronized boolean setFlushingDeleteQueue(DocumentsWriterDeleteQueue session) {
    currentFullFlushDelQueue = session;
    return true;
  }

  private boolean assertTicketQueueModification(DocumentsWriterDeleteQueue deleteQueue) {
    // assign it then we don't need to sync on DW
    DocumentsWriterDeleteQueue currentFullFlushDelQueue = this.currentFullFlushDelQueue;
    assert currentFullFlushDelQueue == null || currentFullFlushDelQueue == deleteQueue:
        "only modifications from the current flushing queue are permitted while doing a full flush";
    return true;
  }
  
  /*
   * FlushAllThreads is synced by IW fullFlushLock. Flushing all threads is a
   * two stage operation; the caller must ensure (in try/finally) that finishFlush
   * is called after this method, to release the flush lock in DWFlushControl
   */
  long flushAllThreads()
    throws IOException {
    final DocumentsWriterDeleteQueue flushingDeleteQueue;
    if (infoStream.isEnabled("DW")) {
      infoStream.message("DW", "startFullFlush");
    }

    long seqNo;

    synchronized (this) {
      pendingChangesInCurrentFullFlush = anyChanges();
      flushingDeleteQueue = deleteQueue;
      /* Cutover to a new delete queue.  This must be synced on the flush control
       * otherwise a new DWPT could sneak into the loop with an already flushing
       * delete queue */
      seqNo = flushControl.markForFullFlush(); // swaps this.deleteQueue synced on FlushControl
      assert setFlushingDeleteQueue(flushingDeleteQueue);
    }
    assert currentFullFlushDelQueue != null;
    assert currentFullFlushDelQueue != deleteQueue;
    
    boolean anythingFlushed = false;
    try {
      DocumentsWriterPerThread flushingDWPT;
      // Help out with flushing:
      while ((flushingDWPT = flushControl.nextPendingFlush()) != null) {
        anythingFlushed |= doFlush(flushingDWPT);
      }
      // If a concurrent flush is still in flight wait for it
      flushControl.waitForFlush();  
      if (anythingFlushed == false && flushingDeleteQueue.anyChanges()) { // apply deletes if we did not flush any document
        if (infoStream.isEnabled("DW")) {
          infoStream.message("DW", Thread.currentThread().getName() + ": flush naked frozen global deletes");
        }
        assertTicketQueueModification(flushingDeleteQueue);
        ticketQueue.addDeletes(flushingDeleteQueue);
      }
      // we can't assert that we don't have any tickets in teh queue since we might add a DocumentsWriterDeleteQueue
      // concurrently if we have very small ram buffers this happens quite frequently
      assert !flushingDeleteQueue.anyChanges();
    } finally {
      assert flushingDeleteQueue == currentFullFlushDelQueue;
    }
    if (anythingFlushed) {
      return -seqNo;
    } else {
      return seqNo;
    }
  }
  
  void finishFullFlush(boolean success) throws IOException {
    try {
      if (infoStream.isEnabled("DW")) {
        infoStream.message("DW", Thread.currentThread().getName() + " finishFullFlush success=" + success);
      }
      assert setFlushingDeleteQueue(null);
      if (success) {
        // Release the flush lock
        flushControl.finishFullFlush();
      } else {
        flushControl.abortFullFlushes();
      }
    } finally {
      pendingChangesInCurrentFullFlush = false;
      applyAllDeletes(deleteQueue); // make sure we do execute this since we block applying deletes during full flush
    }
  }

  @Override
  public long ramBytesUsed() {
    return flushControl.ramBytesUsed();
  }

  /**
   * Returns the number of bytes currently being flushed
   *
   * This is a subset of the value returned by {@link #ramBytesUsed()}
   */
  public long getFlushingBytes() {
    return flushControl.getFlushingBytes();
  }
}
