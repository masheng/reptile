package com.books.utils;

import com.books.utils.DownModel;
import com.company.core.utils.D;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 所有实例数据的基类
 */
public class InfoModel {
    public String pageUrl;  //原始页面地址
    public Set<DownModel> downModel = new HashSet<>();

    public String bookName;
    public String bookAuthor;
    public String bookType;
    public String bookFormat;   //书的格式 pdf/epub

    public InfoModel() {
    }

    public void addDownModel(DownModel model) {
        model.pageUrl = this.pageUrl;
        this.downModel.add(model);
    }

    public boolean save() {
        return BookDBUtls.insertOrUpBook(this);
    }

    @Override
    public String toString() {
        return "InfoModel{" +
                "pageUrl='" + pageUrl + '\'' +
                ", downModel='" + (downModel == null ? "" : downModel.toString()) + '\'' +
                ", bookName='" + bookName + '\'' +
                ", bookType='" + bookType + '\'' +
                ", bookFormat='" + bookFormat + '\'' +
                '}';
    }
}
