package com.books.weijing;

import com.books.utils.*;
import com.company.core.App;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//https://vikingcabin.com/category/pdf/?price=all
public class WeiJingApp extends BookApp {
    private static final String CTFILE = "https://n459.com/";

    @Override
    public void config() {
        super.config();
        TaskModel taskModel = createTask(HOME);
        taskModel.url = "https://vikingcabin.com/category/pdf/?price=all";
        addHttpTask(taskModel);
    }

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case HOME:
                parseHome(task);
                break;
            case LIST:
                parseList(task);
                break;
            case INFO:
                parseInfo(task);
                break;


        }
    }

    private void parseHome(TaskModel task) {
        parseList(task);

        Element pageEle = task.resDoc.selectFirst("body > section > div.pagination.pagination-multi > ul > li:nth-last-child(2) > a");
        String pageText = pageEle.text();
        String pageCountUrl = pageEle.attr("href");

        if (!"尾页".equals(pageText) || StrUtils.isEmpty(pageCountUrl)) {
            throw new RuntimeException("WeiJingApp 获取总页数失败");
        }
        //<a href="https://vikingcabin.com/category/pdf/page/131/?price=all">尾页</a>
        String pageCount = StrUtils.subStr(pageCountUrl, "page/", "/?price", false);
        int count = Integer.parseInt(pageCount);

        scanInfoModel.cateInfo.put(DEFAULT_SCAN_CATE, new ScanInfoModel.ScanInfo(DEFAULT_SCAN_CATE, count));
        count = pageCountOff(count, DEFAULT_SCAN_CATE);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = String.format("https://vikingcabin.com/category/pdf/page/%d/?price=all", i + 1);
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Document doc = task.resDoc;
        Elements urls = doc.select("#posts > article > a");
        for (Element url : urls) {
            TaskModel taskModel = createTask(INFO);
            taskModel.url = url.attr("href");

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        Document doc = task.resDoc;
        Elements urls = doc.select("body > section.container > div.content-wrap > div > article > p > a");
        InfoModel model = new InfoModel();
        model.pageUrl = task.url;
//<span style="font-size: 18pt;">点击下载：中华文明历史长卷：自是林泉多蕴藉(园林卷) PDF下载</span>
        for (int i = urls.size() - 1; i >= 0; i--) {
            Element u = urls.get(i);
            String name = u.text();
            String url = u.attr("href");
            model.addDownModel(new DownModel(url, url.startsWith(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

            if (name.contains("点击下载")) {
                model.bookName = StrUtils.subStr(name, "点击下载：", "PDF下载", true).trim();
                if (name.contains("PDF"))
                    model.bookFormat = BookConstant.F_PDF;
                break;
            }
        }

        D.p("wj==>" + model.toString());

        saveBook(model);
    }

    public static void main(String[] args) {
        WeiJingApp app = new WeiJingApp();
        app.startSingle();
    }
}
