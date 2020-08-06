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

    protected int pageCountOff(int currentCount, String cate) {
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
        if (!ret)
            D.e(SITE + "  保存扫描数据失败==>" + scanInfoModel.toString());
        return ret;
    }

    protected void saveBook(InfoModel model) {
        if (model.save()) {
            scanInfoModel.bookAdd.incrementAndGet();
            D.i("保存电子书成功:" + model.toString());
        } else
            D.e("保存电子书数据失败:" + model.toString());
    }
}
