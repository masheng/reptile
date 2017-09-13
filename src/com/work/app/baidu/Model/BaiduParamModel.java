package com.work.app.baidu.Model;

import com.company.core.App;
import com.company.core.STEP;
import com.company.core.TaskModel;

/**
 * Created by ms on 2017/9/13.
 */
public class BaiduParamModel extends TaskModel implements Cloneable{
    public BaiduParamModel(){}
    public BaiduParamModel(String url, App app, STEP step) {
        super(url, app, step);
    }

    public String category;//大类
    public String subcategory;//小类
    public int index;   //页数
    public String boardid;
    public String subcatesoft;
    public int tag;//标记唯一的小类别 用于处理页数并发

    @Override
    public String toString() {
        return "BaiduParamModel{" +
                "cate='" + category + '\'' +
                ", subCate='" + subcategory + '\'' +
                ", index=" + index +
                ", tag=" + tag +
                ", boardid='" + boardid + '\'' +
                ", subcatesoft='" + subcatesoft + '\'' +
                '}'+super.toString();
    }

    @Override
    public Object clone() {
        return super.clone();
    }
}
