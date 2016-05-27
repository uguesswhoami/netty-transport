package com.netty.transport.mockbio;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorExtention extends ThreadPoolExecutor{

    public ThreadPoolExecutorExtention(int nThreads){
        super(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private long time;

    @Override
    public void beforeExecute(Thread t, Runnable r){
        System.out.println("task start...");
        super.beforeExecute(t, r);
        time = System.currentTimeMillis();
    }

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        System.out.println("task end... time is: " + (System.currentTimeMillis() - time));
        super.afterExecute(r, t);
    }
}