package com.work.books.apps;

import com.work.books.utils.*;
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
        count = pageCountOff(count, DEFAULT_SCAN_CATE, task.url);

        //<a href="http://www.ssylu.com/page/621" original-title="最末页">621</a>

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = "http://www.ssylu.com/page/" + i;
//            D.i("home==>" + taskModel.url);
            addHttpTask(taskModel);

            if (i == 4)
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
            InfoModel infoModel = new InfoModel();

            if (title.contains("《"))
                infoModel.bookName = StrUtils.subStr(title, "《", "》", true);
            else
                infoModel.bookName = title;

            if (title.contains("."))
                infoModel.bookFormat = title.substring(title.lastIndexOf(".") + 1);
            if (title.contains("》"))
                infoModel.bookAuthor = title.substring(title.lastIndexOf("》") + 1, title.lastIndexOf("."));

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
        if (downEle == null)
            return;
        String downUrl = downEle.attr("href");
        task.infoModel.pageUrl = task.url;
        task.infoModel.addDownModel(new DownModel(downUrl, downUrl.startsWith(CTFILE)
                ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

        if (D.DEBUG)
            D.i("ssyl==>" + task.infoModel.toString());

        saveBook(task.infoModel);
    }
}
