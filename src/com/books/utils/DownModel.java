package com.books.utils;


import java.util.Objects;

public class DownModel {
    public String pageUrl;
    private String downUrl;
    public String urlMD5;
    public String code;
    public int type;    //网盘类型
    public String redundancy;   //冗余字段

    public DownModel() {
    }

    public DownModel(String downUrl, String code, int type) {
        this.downUrl = downUrl;
        this.code = code;
        this.type = type;

        createMD5();
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
        createMD5();
    }

    public String getDownUrl() {
        return this.downUrl;
    }

    private void createMD5() {
        if (!StrUtils.isEmpty(downUrl) || downUrl.length() > 5)
            this.urlMD5 = MD5Utils.strToMD5(downUrl);
    }

    public DownModel(String downUrl, int type) {
        this(downUrl, "", type);
    }

    public DownModel(String downUrl) {
        this(downUrl, "", BookConstant.PRIVATE_PAN);
    }

    @Override
    public String toString() {
        return "DownModel{" +
                "downUrl='" + downUrl + '\'' +
                ", urlMD5='" + urlMD5 + '\'' +
                ", code='" + code + '\'' +
                ", type=" + type +
                ", redundancy='" + redundancy + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownModel downModel = (DownModel) o;

        return Objects.equals(downUrl, downModel.downUrl);
    }

    @Override
    public int hashCode() {
        return downUrl != null ? downUrl.hashCode() : 0;
    }
}
