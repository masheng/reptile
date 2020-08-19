package com.work.books.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * 所有实例数据的基类
 */
public class InfoModel implements Cloneable {
    public String pageUrl;  //原始页面地址
    public Set<DownModel> downModel = new HashSet<>();

    public String bookName = "";
    public String bookAuthor = "";
    public String bookType = "";
    public String bookFormat = "";   //书的格式 pdf/epub
    public String bookImg = "";
    public String bookDesc = "";
    public String password = "";

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
    public InfoModel clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        InfoModel model = (InfoModel) obj;
        model.downModel = new HashSet<>();

        return model;
    }

    @Override
    public String toString() {
        return "InfoModel{" +
                "pageUrl='" + pageUrl + '\'' +
                ", downModel='" + (downModel == null ? "" : downModel.toString()) + '\'' +
                ", bookName='" + bookName + '\'' +
                ", bookAuthor='" + bookAuthor + '\'' +
                ", bookType='" + bookType + '\'' +
                ", bookFormat='" + bookFormat + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
