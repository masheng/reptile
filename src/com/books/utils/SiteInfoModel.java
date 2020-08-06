package com.books.utils;

public class SiteInfoModel {
    public String url;
    public String md5;

    public SiteInfoModel(String url, String md5) {
        this.url = url;
        this.md5 = md5;
    }

    public SiteInfoModel() {
    }

    @Override
    public String toString() {
        return "SiteInfoModel{" +
                "url='" + url + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
