package com.company.core;

import com.company.core.utils.ProxyTestModel;

import java.util.Map;

/**
 * Created by ms on 2017/9/9.
 */
public class TaskModel {
    //步骤
    public transient STEP step;
    //状态值 用于判断是否需要代理
    public transient int status;
    //自定义使用
    public transient int arg;

    //失败重试
    public transient int reTryConnCount;
    public transient int reTryReadCount;

    public transient App app;

    public transient String url;
    //http消息头
    public transient java.util.Map<String,String> headers;
    //post上传参数
    public transient Map<String,String> params;
    //post get
    public transient String kind;
    //网络请求返回的结果
    public transient String result;
    //失败原因
    public transient String errMsg;

    public transient ProxyTestModel proxy;

    public TaskModel(String url, App app, STEP step) {
        this.url = url;
        this.app = app;
        this.step = step;
    }

    public TaskModel() {}

    @Override
    public String toString() {
        return "TaskModel{" +
                "step=" + step +
                ", arg=" + arg +
                ", reTryConnCount=" + reTryConnCount +
                ", reTryReadCount=" + reTryReadCount +
                ", app=" + app +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", params=" + params +
                ", kind='" + kind + '\'' +
                ", result='" + result + '\'' +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }
}
