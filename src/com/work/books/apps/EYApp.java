package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//风陵渡书屋
public class EYApp extends BookApp {
    public static void main(String[] args) {
        EYApp app = new EYApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel task1 = createTask(CATEGORY);
        task1.url = "http://www.eybook.com/index.php?c=category&id=7";
        task1.cate = "畅销图书";
        addHttpTask(task1);
        TaskModel task2 = createTask(CATEGORY);
        task2.url = "http://www.eybook.com/index.php?c=category&id=8";
        task2.cate = "文学小说";
        addHttpTask(task2);
    }

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case CATEGORY:
                parseCate(task);
                break;
            case LIST:
                parseList(task);
                break;
            case INFO:
                parseInfo(task);
                break;
        }
    }

    private void parseCate(TaskModel task) {
        Elements pageUrlEles = task.resDoc.select("#wrapper > div > section > div > div.pager > ul > li > a");
        String pageCount = "";
        Element lastPageEle = pageUrlEles.get(pageUrlEles.size() - 1);
        if (lastPageEle.text().contains("最后一页")) {
            pageCount = lastPageEle.attr("data-ci-pagination-page");
        } else {
            Element lastPageEle1 = pageUrlEles.get(pageUrlEles.size() - 2);
            pageCount = lastPageEle1.attr("data-ci-pagination-page");
        }
        int count = Integer.parseInt(pageCount);
        count = cateCount(task.cate, task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = task.url + "&page=" + i;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Elements listEles = task.resDoc.select("#wrapper > div > section > div > div.lister > dl > dd > div > a");
        for (Element e : listEles) {
            TaskModel taskModel = createTask(INFO);
            taskModel.testSite = true;
            taskModel.url = e.attr("href");
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        Element infoEle = task.resDoc.selectFirst("#wrapper > div > section > div > div.book-introduce > div.book-profile > div.exp");

        String bookName = infoEle.selectFirst("h1").text();
        //<h1>朱元璋传【吴晗】eybook.com</h1>
        if (bookName.contains("【")) {
            bookName = bookName.substring(0, bookName.indexOf("【")).trim();
        }
        String bookAuthor = "";
        Elements proEles = infoEle.select("dl.property > dd");
        for (Element e : proEles) {
            if (e.text().contains("作者")) {
                bookAuthor = e.text().split("：")[1];
            }
        }

        InfoModel infoModel = new InfoModel();
        infoModel.pageUrl = task.url;
        infoModel.bookName = bookName;
        infoModel.bookAuthor = bookAuthor;

        Elements downEles = task.resDoc.select("#d > div.main > div.book-download > dl.less > dd > label");
        String downUrl = downEles.get(0).text();
        infoModel.bookFormat = downEles.get(1).text();
        String code = downEles.get(2).text();
        //<a href="https://pan.baidu.com/s/1HnwUdia1F-qfps9tEN_fUQ " target="_blank">点击下载</a>
        int type = downUrl.contains("pan.baidu") ? BookConstant.BAIDU_PAN : BookConstant.PRIVATE_PAN;
        infoModel.addDownModel(new DownModel(downUrl, code, type));

        saveBook(infoModel);
    }
}
