package com.netty.transport.mockbio;

import java.util.concurrent.Executor;

public class ThreadExecutorsUtil{

    private static Executor executor = new ThreadPoolExecutorExtention(20);

    public static void execute(Runnable runnable){
        executor.execute(runnable);
    }
}