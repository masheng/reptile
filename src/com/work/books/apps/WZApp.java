package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//丸子学英语  https://www.wanzi.pw
public class WZApp extends BookApp {
    private static final String CTFILE = "https://474b.com";

    public static void main(String[] args) {
        WZApp app = new WZApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();
        TaskModel task = createTask(HOME);
        task.url = "https://www.wanzi.pw";
        addHttpTask(task);
    }

    @Override
    public void parse(TaskModel task) {
//        D.d("parse==>" + task.tag);
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

        Element countPageEle = task.resDoc.selectFirst("#main > ol.page-navigator > li:nth-last-child(2) > a");
        String countPage = countPageEle.text();
//        D.ee("==>" + countPage);
        int count = Integer.parseInt(countPage);

        scanInfoModel.cateInfo.put(DEFAULT_SCAN_CATE, new ScanInfoModel.ScanInfo(DEFAULT_SCAN_CATE, count));
        count = pageCountOff(count, DEFAULT_SCAN_CATE, task.url);

        //<a href="https://www.wanzi.pw/page/2/">下一页</a>
        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = String.format("https://www.wanzi.pw/page/%d/", i);
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Elements itemELes = task.resDoc.select("#main > article > div > div.post-body > h2.post-title > a");
        for (Element e : itemELes) {
            TaskModel taskModel = createTask(INFO);
            taskModel.url = e.attr("href");

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        //<a href="https://474b.com/file/3755530-453098929" target="_blank">康德认识论文集（注释版）.azw3</a>
        Elements infoEles = task.resDoc.select("#article-content > p > a");
        for (Element e : infoEles) {
            String title = e.text();
            String bookName = title.substring(0, title.lastIndexOf("."));
            String bookFormat = title.substring(title.lastIndexOf(".") + 1);
            String url = e.attr("href");

            InfoModel infoModel = new InfoModel();
            infoModel.pageUrl = task.url;
            infoModel.bookName = bookName;
            infoModel.bookFormat = bookFormat;
            infoModel.addDownModel(new DownModel(url, url.startsWith(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

            if (D.DEBUG)
                D.i("WZ==>" + infoModel);

            saveBook(infoModel);

            if (D.DEBUG)
                break;
        }
    }
}
