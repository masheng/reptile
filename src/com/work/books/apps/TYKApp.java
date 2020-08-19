package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//https://www.chinjua.com/archives/category/电子书/电子书更新/
public class TYKApp extends BookApp {
    private static final String CTFILE = "https://n802.com";

    public static void main(String[] args) {
        TYKApp app = new TYKApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel task = createTask(HOME);
        task.url = "https://www.chinjua.com/archives/category/电子书/电子书更新/";
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
            case DOWN:
                parseDown(task);
                break;
        }
    }

    private void parseHome(TaskModel task) {
        parseList(task);

        Element pageCountEle = task.resDoc.selectFirst("#primary > div > nav > div > a:nth-last-child(2)");
        String countUrl = pageCountEle.attr("href");
        int subEnd = countUrl.lastIndexOf("/");
        int subStart = countUrl.lastIndexOf("/", subEnd - 1);
        String countPage = countUrl.substring(subStart + 1, subEnd);
        int count = Integer.parseInt(countPage);

        scanInfoModel.cateInfo.put(DEFAULT_SCAN_CATE, new ScanInfoModel.ScanInfo(DEFAULT_SCAN_CATE, count));
        count = pageCountOff(count, DEFAULT_SCAN_CATE, task.url);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = String.format("https://www.chinjua.com/archives/category/电子书/电子书更新/page/%d/", i);
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Elements itmeEles = task.resDoc.select("#main > article > header > h2.entry-title > a");
        for (Element e : itmeEles) {
            String url = e.attr("href");

            if (BookDBUtls.testSaveSiteInfo(url)) {
                TaskModel taskModel = createTask(INFO);
                taskModel.url = url;
                addHttpTask(taskModel);
            }
            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        Elements urlEles = task.resDoc.select("#main > article > div.entry-content > div.single-content > div.mark_e.mark > table > tbody > tr > td.column-2 > a");
        for (Element e : urlEles) {
            String url = e.attr("href");
            String title = e.text();
            String bookName = title;
            String bookAuthor = "";
            //<a rel="external nofollow" target="_blank" href="https://www.chinjua.com/wp-content/themes/begin/go.php?url=aHR0cHM6Ly81NDVjLmNvbS9maWxlLzc4MjMwMzYtNDE1MzA4MDkw">语言教学原理(盛炎)</a>
            if (title.contains("(") && title.contains(")")) {
                bookAuthor = StrUtils.subStr(title, "(", ")", true);
                bookName = title.substring(0, title.indexOf("("));
            } else if (title.contains("[") && title.contains("]")) {
                bookAuthor = StrUtils.subStr(title, "[", "]", true);
                bookName = title.substring(0, title.indexOf("["));
            }

            InfoModel infoModel = new InfoModel();
            infoModel.pageUrl = task.url;
            infoModel.bookName = bookName;
            infoModel.bookAuthor = bookAuthor;
            infoModel.bookFormat = BookConstant.F_PDF;

            TaskModel taskModel = createTask(DOWN);
            taskModel.url = url;
            taskModel.infoModel = infoModel;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseDown(TaskModel task) {
        Element downEle = task.resDoc.selectFirst("#alert-box > div.alert-concent > div.alert-url");
        String downUrl = downEle.text();
        task.infoModel.pageUrl = task.url;
        task.infoModel.addDownModel(new DownModel(downUrl, downUrl.startsWith(CTFILE)
                ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));
        D.ee("downUrl==>" + downUrl);

        saveBook(task.infoModel);
    }

}
