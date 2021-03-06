package com.work.books.apps;

import com.work.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SXTXApp extends BookApp {
    private static final String CTFILE = "https://tc5.us/";

    public static void main(String[] args) {
        SXTXApp app = new SXTXApp();
        app.start();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel taskModel = createTask(HOME);
        taskModel.url = "https://www.sxpdf.com";
        addHttpTask(taskModel);
    }

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case HOME:
                parseHome(task);
                break;
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

    private void parseInfo(TaskModel task) {
        Elements downEles = task.resDoc.select("#main > article > div > div.single-content > div.down-form > fieldset > span.down a");
        for (Element down : downEles) {
            String url = down.attr("href");
            task.infoModel.addDownModel(new DownModel(url, url.startsWith(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));
        }

        if (D.DEBUG)
            D.i("书行天下==>" + task.infoModel.toString());

        saveBook(task.infoModel);
    }

    private void parseList(TaskModel task) {
        Elements list = task.resDoc.select("#main > article > header > h2 > a");
        for (Element item : list) {
            String url = item.attr("href");
            String bookName = item.text();

            InfoModel model = new InfoModel();
            model.bookName = bookName;
            model.pageUrl = url;
            if (bookName.contains("《") && bookName.contains("》")) {
                model.bookName = StrUtils.subStr(bookName, "《", "》", true);
            } else if (bookName.contains("PDF")) {
                model.bookName = bookName.substring(0, bookName.indexOf("PDF"));
                if (model.bookName.contains("《"))
                    model.bookName.replace("《", "");
            }
            if (bookName.contains("PDF")) {
                model.bookFormat = BookConstant.F_PDF;
            }

            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            taskModel.infoModel = model;

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    //https://www.sxpdf.com/lishi/
    //https://www.sxpdf.com/lishi/page/3/
    private void parseCate(TaskModel task) {
        parseList(task);
        //获取总页数
        Elements pageEles = task.resDoc.select("#primary > div > nav.navigation.pagination > div > a");
        String pageCountUrl = "";
        try {
            pageCountUrl = pageEles.get(pageEles.size() - 2).attr("href");
        } catch (Exception e) {
            D.e("==>" + task.toString());
            e.printStackTrace();
        }
        int startIndex = pageCountUrl.lastIndexOf("page/") + "page/".length();
        String pageCout = pageCountUrl.substring(startIndex, pageCountUrl.lastIndexOf('/'));
        int count = Integer.parseInt(pageCout);

        String cateMd5 = MD5Utils.strToMD5(task.cate);
        scanInfoModel.cateInfo.put(cateMd5, new ScanInfoModel.ScanInfo(task.cate, count));
        count = pageCountOff(count, cateMd5, task.url);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = String.format("%s/page/%d/", task.url, i);
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseHome(TaskModel task) {
        Elements cateEles = task.resDoc.select("#site-nav > div > ul > li:not(:first-child) > a");
        for (Element cate : cateEles) {
            String url = cate.attr("href");
            //排除视频教程
            if (url.contains("spjc"))
                break;

            String cateName = cate.text();

            TaskModel taskModel = createTask(CATEGORY);
            taskModel.url = url;
            taskModel.cate = cateName;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }
}
