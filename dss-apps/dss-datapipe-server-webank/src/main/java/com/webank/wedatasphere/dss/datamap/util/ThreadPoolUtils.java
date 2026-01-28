package com.webank.wedatasphere.dss.datamap.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtils {

    /**
     *
     * @param threadName 线程名称前缀
     * @param isDaemon 守护线程
     * @return ThreadFactory
     */
    private static ThreadFactory threadFactory(String threadName, boolean isDaemon) {
        return new ThreadFactory() {
            AtomicInteger num = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(isDaemon);
                t.setName(threadName + "-" + num.incrementAndGet());
                return t;
            }
        };
    }

    /**
     * 创建线程池，其中的核心线程可缓存一定时间，核心线程在空闲keepAliveTime=120L SECONDS时间后核心线程会被销毁，再次向线程池提交任务会创建新的核心线程
     * @param threadNum 线程数
     * @param threadName 线程名称前缀
     * @param isDaemon 默认true
     * @return
     */
    public static ThreadPoolExecutor newCachedThreadPool(int threadNum, String threadName, Boolean isDaemon) {
        if (isDaemon == null) {
            isDaemon = true;
        }
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(threadNum, threadNum,
                120L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory(threadName, isDaemon)
        );
        threadPool.allowCoreThreadTimeOut(true);
        return threadPool;
    }

}
