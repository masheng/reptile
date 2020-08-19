package com.work.books.utils;

import com.company.core.App;
import com.company.core.utils.D;

import java.util.Map;

public abstract class BookApp extends App {
    protected final static String HOME = "HOME";
    protected final static String CATEGORY = "category";
    protected static final String LIST = "list";
    protected static final String ITEM = "item";
    protected static final String INFO = "info";
    protected static final String DOWN = "down";
    protected String SITE;

    protected long startTime;

    public static final String DEFAULT_SCAN_CATE = "scan_cate";

    protected Map<String, ScanInfoModel.ScanInfo> lastScanCateInfo;
    protected ScanInfoModel scanInfoModel = new ScanInfoModel();

    public BookApp() {
        SITE = this.getClass().getSimpleName();

        threadUtils.setThreadCallback(() -> {
            saveScanInfo();
        });
    }

    @Override
    protected void config() {
        lastScanCateInfo = BookDBUtls.getScanInfo(SITE);
        startTime = System.currentTimeMillis();

        D.i(String.format("%s 上次扫描参数为:%s", SITE, lastScanCateInfo == null ? "null" : lastScanCateInfo.toString()));
    }

    protected int pageCountOff(int currentCount, String cate, String url) {
        if (D.DEBUG)
            return currentCount;

        if (currentCount <= 0)
            D.e(String.format("cate:%d  pageCount:%d  url:%s", cate, currentCount, url));

        int scanCount = currentCount;
        int lastPage = 0;
        if (lastScanCateInfo != null && !lastScanCateInfo.isEmpty()) {
            ScanInfoModel.ScanInfo info = lastScanCateInfo.get(cate);
            if (info != null) {
                if (currentCount >= info.lastPage) {
                    scanCount = (currentCount - info.lastPage + 1);
                    lastPage = info.lastPage;
                }
            }
        }

        D.i(String.format("%s 上次页数:%d  此次页数:%d  预计扫描%d", SITE, lastPage, currentCount, scanCount));

        return scanCount;
    }

    public boolean saveScanInfo() {
        scanInfoModel.site = SITE;
        scanInfoModel.duration = (int) ((System.currentTimeMillis() - startTime) / 1000);
        D.i(SITE + "  保存扫描数据==>" + scanInfoModel.toString());

        boolean ret = BookDBUtls.setScanInfo(scanInfoModel);
        if (!ret) {
            String scan = scanInfoModel.toString();
            D.e(SITE + "  保存扫描数据失败==>" + scan.length() + "--" + scan);
        }
        return ret;
    }

    protected void saveBook(InfoModel model) {
        if (model.save()) {
            scanInfoModel.bookAdd.incrementAndGet();
//            D.i("保存电子书成功:" + model.toString());
        } else
            D.e("保存电子书数据失败:" + model.toString());
    }

    protected int cateCount(String cate, String url, int count) {
        String cateMd5 = MD5Utils.strToMD5(cate);
        scanInfoModel.cateInfo.put(cateMd5, new ScanInfoModel.ScanInfo(cate, count));
        return pageCountOff(count, cateMd5, url);
    }

    protected int cateCountDefault(String url, int count) {
        scanInfoModel.cateInfo.put(DEFAULT_SCAN_CATE, new ScanInfoModel.ScanInfo(DEFAULT_SCAN_CATE, count));
        return pageCountOff(count, DEFAULT_SCAN_CATE, url);
    }

    /**
     * 如果标题中包含电子书的格式 将标题中的电子书格式分离
     * <p>
     * [0] 所有的电子书格式
     * [1] 第一种电子书格式 用于抽出书名
     */
    public String[] testFormat(String title) {
        title = title.toLowerCase();
        StringBuilder format = new StringBuilder();
        String[] info = new String[2];
        int front = title.length();
        if (title.contains(BookConstant.F_EPUB)) {
            format.append(BookConstant.F_EPUB);
            int index = title.indexOf(BookConstant.F_EPUB);
            if (index < front) {
                info[1] = BookConstant.F_EPUB;
                front = index;
            }
        }
        if (title.contains(BookConstant.F_MOBI)) {
            if (format.length() != 0)
                format.append("/");
            format.append(BookConstant.F_MOBI);
            int index = title.indexOf(BookConstant.F_MOBI);
            if (index < front) {
                info[1] = BookConstant.F_MOBI;
                front = index;
            }
        }
        if (title.contains(BookConstant.F_AZW3)) {
            if (format.length() != 0)
                format.append("/");
            format.append(BookConstant.F_AZW3);
            int index = title.indexOf(BookConstant.F_AZW3);
            if (index < front) {
                info[1] = BookConstant.F_AZW3;
                front = index;
            }
        }
        if (title.contains(BookConstant.F_TXT)) {
            if (format.length() != 0)
                format.append("/");
            format.append(BookConstant.F_TXT);
            int index = title.indexOf(BookConstant.F_TXT);
            if (index < front) {
                info[1] = BookConstant.F_TXT;
                front = index;
            }
        }
        if (title.contains(BookConstant.F_PDF)) {
            if (format.length() != 0)
                format.append("/");
            format.append(BookConstant.F_PDF);
            int index = title.indexOf(BookConstant.F_PDF);
            if (index < front) {
                info[1] = BookConstant.F_PDF;
                front = index;
            }
        }

        info[0] = format.toString();
        return info;
    }
}
