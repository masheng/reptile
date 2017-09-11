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
        HttpUtils.getResult(task);
    }
}
