package com.hjh.java.Thread;

import net.openhft.chronicle.core.util.Histogram;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description:
 */

public class LockVsSync {
    private static final boolean COORDINATED_OMISSION = Boolean.getBoolean("coordinatedOmission");
    //Either run testing Lock or testing synchronized
    private static final boolean IS_LOCK = Boolean.getBoolean("isLock");
    private static final int NUM_THREADS = Integer.getInteger("numThreads",50);

    @Test
    public void test() throws InterruptedException {
        Lock lock = new ReentrantLock();
        for (int t = 0; t < NUM_THREADS ;t++){
            if (t == 0) {
                //Set the first thread as the master which will be measured
                //设置第一个线程作为测量的线程
                //The other threads are only to cause contention
                //其他线程只是引起竞争
                Runner r = new Runner(lock, true);
                r.start();
            } else {
                Runner r = new Runner(lock, false);
                r.start();
            }
        }

        synchronized (this) {
            //Hold the main thread from completing
            wait();
        }

    }

    private void testLock(Lock rlock) {
        rlock.lock();
        try {
            for (int i = 0; i < 2 ; i++){
                double x = 10 / 4.5 + i;
            }
        } finally {
            rlock.unlock();
        }
    }

    private synchronized void testSync() {
        for (int i = 0; i < 2 ;i++){
            double x = 10 / 4.5 + i;
        }
    }

    class Runner extends Thread {
        private Lock lock;
        private boolean master;

        public Runner(Lock lock, boolean master) {
            this.lock = lock;
            this.master = master;
        }

        @Override
        public void run() {
            Histogram histogram = null;
            if (master)
                histogram = new Histogram();

            long rate = 1000;//expect 1 every microsecond
            long now = 0;
            for (int i = -10000; i<0 ;i++){
                if (!COORDINATED_OMISSION) {
                    now += rate;
                    while (System.nanoTime() == 0 && master){
                        histogram.sample(System.nanoTime() - now);
                    }
                }
                if (master) {
                    System.out.println(histogram.toMicrosFormat());
                    System.exit(0);
                }
            }
        }
    }
}