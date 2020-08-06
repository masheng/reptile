package com.books.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 扫描记录
 */
public class ScanInfoModel {
    public AtomicInteger bookAdd = new AtomicInteger(0); //新增数量
    public String site;
    public int duration;
    public Map<String, ScanInfo> cateInfo = new HashMap<>();

    public static class ScanInfo {
        public String category;
        public int lastPage;

        public ScanInfo() {
        }

        public ScanInfo(String category, int lastPage) {
            this.category = category;
            this.lastPage = lastPage;
        }

        @Override
        public String toString() {
            return "ScanInfoModel{" +
                    "category='" + category + '\'' +
                    ", lastPage=" + lastPage +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ScanInfoModel{" +
                "bookAdd=" + bookAdd +
                ", site='" + site + '\'' +
                ", duration=" + duration +
                ", cateInfo=" + cateInfo +
                '}';
    }
}
