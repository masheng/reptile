package com.company.core;

/**
 * Created by ms on 2017/7/31.
 */
public interface IParse {
    /**
     * 解析文本操作
     * */
    void parse(TaskModel taskModel);

    /**
     * 失败操作
     * */
    void failed(TaskModel taskModel);
}
