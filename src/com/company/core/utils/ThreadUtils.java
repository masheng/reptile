package com.company.core.utils;

import com.company.core.model.TaskModel;

import java.util.concurrent.*;

public class ThreadUtils {
    private ThreadPoolExecutor threadPool;
    private int flag = 0;
    private ThreadCallback threadCallback;

    public void init() {
        init(new Config());
    }

    public void init(Config config) {
        threadPool = new ThreadPoolExecutor(config.DEFAULT_CORE_THREAD_SIZE, config.DEFAULT_MAX_THREAD_SIZE, 30,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(config.DEFAULT_QUEUE_MAX_SIZE)
                , new RejectedExecutionHandler() {

            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                //将处理不了的返回队列
                D.e("==>rejectedExecution");
            }
        });
    }

    public void destory() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    public boolean addTask(TaskModel task) {
        if (threadPool == null || threadPool.isShutdown())
            return false;

        flag = 0;
        threadPool.submit(() -> {
            try {
                HttpUtils.disposeTask(task);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        });
        return true;
    }

    public void log() {
        if (threadPool == null) {
            System.out.println("线程已经关闭");
        }
        System.out.println("------------------------------############--------------------");
        System.out.println("曾计划执行的近似任务总数:" + threadPool.getTaskCount());
        System.out.println("已完成执行的近似任务总数:" + threadPool.getCompletedTaskCount());
        System.out.println("池中曾出现过的最大线程数:" + threadPool.getLargestPoolSize());
        System.out.println("返回线程池中的当前线程数:" + threadPool.getPoolSize());
        System.out.println("线程池中的当前活动线程数:" + threadPool.getActiveCount());
        System.out.println("线程池中约定的核心线程数:" + threadPool.getCorePoolSize());
        System.out.println("线程池中约定的最大线程数:" + threadPool.getMaximumPoolSize());
        System.out.println("当前剩余任务数:" + threadPool.getQueue().size());
    }

    public boolean waitFinish() {
        long time = System.currentTimeMillis();
        boolean ret;
        while (true) {
            if (threadPool.getQueue().size() == 0 && threadPool.getActiveCount() == 0) {
                if (flag > 1) {
                    threadPool.shutdown();
                    ret = true;
                    break;
                }

                flag++;
            } else
                flag = 0;

//            D.p(String.format("queue size:%d  thread count:%d  during s:%d", threadPool.getQueue().size(), threadPool.getActiveCount(), (System.currentTimeMillis() - time) / 1000));
            log();

            try {
                Thread.currentThread().sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (this.threadCallback != null)
            this.threadCallback.onFinish();

        return ret;
    }

    public void setThreadCallback(ThreadCallback callback) {
        this.threadCallback = callback;
    }

    public static class Config {
        public int DEFAULT_CORE_THREAD_SIZE = 20;
        public int DEFAULT_QUEUE_MAX_SIZE = 5000;
        public int DEFAULT_MAX_THREAD_SIZE = DEFAULT_CORE_THREAD_SIZE * 10;
    }

    public interface ThreadCallback {
        void onFinish();
    }
}
