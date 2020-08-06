package com.company.core.model;

import com.books.utils.InfoModel;
import com.company.core.App;
import org.jsoup.nodes.Document;

import java.util.Map;

public class TaskModel {
    public static final String UTF8 = "utf-8";
    public static final String GB2312 = "gb2312";

    public TaskModel() {
    }

    public TaskModel(App app, String tag) {
        this.app = app;
        this.tag = tag;
    }

    public void clear() {
        reTryConnCount = 0;
        reTryReadCount = 0;
        errCode = 0;
        errMsg = null;
    }

    public App app;

    //失败重试
    public transient int reTryConnCount;
    public transient int reTryReadCount;
    public transient int reTryMaxCount = 5;

    //任务的标记
    public transient String tag;
    //每个请求有各自的用途
    public String desc;

    public transient String url;
    //http消息头
    public transient java.util.Map<String, String> headers;
    //post上传参数
    public transient Map<String, String> params;
    //post get
    public transient String requestType;
    //网络请求返回的结果
    public transient String response;
    public transient Document resDoc;
    public String resEncode = UTF8;
    public InfoModel infoModel;
    //失败原因
    public transient int errCode;
    public transient String errMsg;

    //执行一个任务前 延迟的时间 防止过于频繁的请求 ms
    public int delayTime = 0;
}
