package com.company.core;


import com.work.app.app360.App360;

import java.util.concurrent.*;

/**
 * Created by ms on 2017/9/9.
 */
public abstract class App implements IParse {
    private int queueLen;
    private ThreadPoolExecutor threadPool;
    //ArrayBlockingQueue的长度
    protected int queueCap = 3000;
    //核心线程数
    protected int coreThread = 20;
    protected int maxThread = 5*coreThread+1;

    public void addHttpTask(TaskModel task) {
        if(task != null) {
            threadPool.execute(new HttpTask(task));
            debug(task,1);
        }
    }

    public void addParseTask(TaskModel task) {
        if(task != null) {
            threadPool.execute(()->parse(task));
            debug(task, 2);
        }
    }

    private void debug(TaskModel task, int tag) {
//        queueLen = threadPool.getQueue().size();
//        if(queueLen == 0)
//            System.out.println(String.format("tag:%d  step:%d read:%d  conn:%d res:%d url:%s", tag, task.step.ordinal(),
//                    task.reTryReadCount, task.reTryConnCount, task.result==null?-1:task.result.length(), task.url));
    }

    public void init(){
        threadPool = new ThreadPoolExecutor(coreThread, maxThread, 30,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueCap),
                new RejectedExecutionHandler() {

                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        //将处理不了的返回队列
                        App360.log360.error("==>rejectedExecution");
                    }
                });
    }

    public void start() {
        init();
        firstPage();
    }

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

}
