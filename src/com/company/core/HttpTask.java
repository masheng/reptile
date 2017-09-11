package com.company.core;

import com.company.core.utils.HttpUtils;

/**
 * Created by ms on 2017/9/9.
 */
public class HttpTask implements Runnable {
    private TaskModel task;

    public HttpTask(TaskModel task) {
        this.task = task;
    }

    @Override
    public void run() {
//        System.out.println("HttpTask=111=>"+App.atomicInteger.getAndIncrement());
        HttpUtils.getResult(task);
//        System.out.println("HttpTask=222=>"+App.atomicInteger.getAndDecrement());
    }
}
