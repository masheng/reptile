package com.books.ssyl;

import com.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SSYLApp extends BookApp {
    private static final String CTFILE = "https://545c.com/";

    public static void main(String[] args) {
        SSYLApp app = new SSYLApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel task = createTask(HOME);
        task.url = "http://www.ssylu.com";
        addHttpTask(task);
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

        Element pageCountEle = task.resDoc.selectFirst("#primary > div > div.pagenavi > a:last-child");
        String pageCount = pageCountEle.text();
        int count = Integer.parseInt(pageCount);

        scanInfoModel.cateInfo.put(DEFAULT_SCAN_CATE, new ScanInfoModel.ScanInfo(DEFAULT_SCAN_CATE, count));
        count = pageCountOff(count, DEFAULT_SCAN_CATE);

        //<a href="http://www.ssylu.com/page/621" original-title="最末页">621</a>
        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = "http://www.ssylu.com/page/" + i;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    //<a href="http://www.ssylu.com/archives/19635" title="《土狼星》艾伦·斯蒂尔.pdf">《土狼星》艾伦·斯蒂尔.pdf</a>
    private void parseList(TaskModel task) {
        Elements listEle = task.resDoc.select("#primary > ul > li > div.content > h2 > a");
        for (Element item : listEle) {
            String url = item.attr("href");
            if (!BookDBUtls.testSaveSiteInfo(url))
                continue;

            String title = item.text();
            String bookName = StrUtils.subStr(title, "《", "》", true);
            String bookFormat = title.substring(title.lastIndexOf(".") + 1);
            String bookAuthor = title.substring(title.lastIndexOf("》") + 1, title.lastIndexOf("."));

            InfoModel infoModel = new InfoModel();
            infoModel.bookName = bookName;
            infoModel.bookFormat = bookFormat;
            infoModel.bookAuthor = bookAuthor;

            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            taskModel.infoModel = infoModel;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    //http://www.ssylu.com/archives/19525
    private void parseInfo(TaskModel task) {
        Element downEle = task.resDoc.selectFirst("#content > h2 > a");
        String downUrl = downEle.attr("href");
        task.infoModel.pageUrl = task.url;
        task.infoModel.addDownModel(new DownModel(downUrl, downUrl.startsWith(CTFILE)
                ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

        saveBook(task.infoModel);
    }
}
