package com.hjh.java.Thread;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description:
 */
public class ThreadPool {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(10);

        TestCallable test = new TestCallable();
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            String str1 = (String)pool.submit(test).get();
            System.out.println(str1);
            String str2 = (String)pool.submit(test).get();
            System.out.println(str2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            pool.shutdown();
        }

    }
}
class TestCallable implements Callable{
    int i = 0;
    @Override
    public Object call() throws Exception {
        return "string"+i++;
    }
}
