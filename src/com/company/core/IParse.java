package com.company.core;

/**
 * Created by ms on 2017/7/31.
 */
public interface IParse {
    void parse(TaskModel taskModel);

    void failed(TaskModel taskModel);
}
