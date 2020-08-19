package com.work.books.utils;

public class BookConstant {
    public static final String DATA_BASE = "reptile_book";
    public static final String TABLE_BOOK = "book";
    public static final String TABLE_SCAN = "scan_info";

    public static final int SCAN_SITE_VARCHAR_LEN = 500;

    //网盘类型
    public static final int PRIVATE_PAN = 0;   //私有网盘
    public static final int BAIDU_PAN = 1;  //百度网盘
    public static final int CTFILE_PAN = 2;  //ctFile 城通网盘
    public static final int TX_PAN = 3;  //腾讯微盘
    public static final int LZ_PAN = 4;  //蓝奏网盘
    public static final int TY_PAN = 5;  //天翼网盘 需要密码
    public static final int CHANG_PAN = 100;  //实时变化的url
    public static final int UNKOWN = 1000;  //未知

    public static boolean isBaiduPan(String url) {
        return url.contains("pan.baidu");
    }

    //电子书格式
    public static final String F_PDF = "pdf";
    public static final String F_EPUB = "epub";
    public static final String F_MOBI = "mobi";
    public static final String F_AZW3 = "azw3";
    public static final String F_TXT = "txt";
    public static final String F_UNKNOW = "unkown";

}
