package com.company.core.model;

import com.work.books.utils.InfoModel;
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
    //true 则先查数据库是否已经请求过 没请求过则请求并加入数据库 否则不请求网络
    public boolean testSite;
    //是否使用jsoup直接解析
    public boolean parse = true;
    //任务的标记
    public transient String tag;
    public String cate;

    //每个请求有各自的用途
    public String obj;

    public transient String url;
    //http消息头
    public transient java.util.Map<String, String> headers;
    //post上传参数
    public transient Map<String, String> params;
    //post get
    public transient String requestType;
    //用于重定向
    public boolean redirect;
    public String redirectUrl;
    //网络请求返回的结果
    public transient String response;
    public transient Document resDoc;
    public String resEncode = UTF8;
    public InfoModel infoModel;
    //失败原因
    public transient int errCode;
    public transient String errMsg;

    //执行一个任务前 延迟的时间 防止过于频繁的请求 ms
    public int delayTime = 8000;

    @Override
    public String toString() {
        return "TaskModel{" +
                "reTryConnCount=" + reTryConnCount +
                ", reTryReadCount=" + reTryReadCount +
                ", reTryMaxCount=" + reTryMaxCount +
                ", parse=" + parse +
                ", tag='" + tag + '\'' +
                ", cate='" + cate + '\'' +
                ", obj='" + obj + '\'' +
                ", url='" + url + '\'' +
                ", resEncode='" + resEncode + '\'' +
                ", errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }
}
