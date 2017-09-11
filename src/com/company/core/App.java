package com.company.core;


import com.work.app.app360.App360;

import java.util.concurrent.*;

/**
 * Created by ms on 2017/9/9.
 */
public abstract class App implements IParse {
    private int DEFAULT_CORE = 20;
    private int DEFAULT_CAP = 3000;
    private int DEFAULT_MAX = DEFAULT_CORE*10;

    private ThreadPoolExecutor threadPool;

    /**
     * 添加网络请求任务
     * */
    public void addHttpTask(TaskModel task) {
        if(task != null) {
            threadPool.execute(new HttpTask(task));
        }
    }

    //网络请求成功后的回调
    public void addParseTask(TaskModel task) {
        if(task != null) {
            threadPool.execute(()->parse(task));
        }
    }

    public void init(){
        threadPool = new ThreadPoolExecutor(getThreadCore(), getThreadMax(), 30,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(getQueueCap()),
                new RejectedExecutionHandler() {

                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        //将处理不了的返回队列
                        //TODO
                        App360.log360.error("==>rejectedExecution");
                    }
                });
    }

    //开始任务
    public void start() {
        init();
        firstPage();
    }

    /**
     * 子类初始化
     * */
    protected abstract void firstPage();

    int flag=0;
    public boolean finish() {
        long time = System.currentTimeMillis();
        while(true) {
            if(threadPool.getQueue().size()==0 && threadPool.getActiveCount()==0) {
                if(flag > 5) {
                    threadPool.shutdown();
                    return true;
                }

                flag++;
            }else
                flag = 0;
            System.out.println(threadPool.getQueue().size()+"---"+threadPool.getActiveCount()+"--"+(System.currentTimeMillis()-time)/1000);
            try {
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected int getThreadCore(){
        return DEFAULT_CORE;
    }

    protected int getThreadMax(){
        return DEFAULT_MAX;
    }

    protected int getQueueCap(){
        return DEFAULT_CAP;
    }
}
