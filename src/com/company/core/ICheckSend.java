package com.company.core;

/**
 * Created by ms on 2017/9/13.
 */
public interface ICheckSend {
    //发起网络请求前 先确认
    boolean check(TaskModel task);
}
