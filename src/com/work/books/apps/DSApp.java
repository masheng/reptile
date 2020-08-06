package com.work.books.apps;

import com.work.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//读书小站  https://ibooks.org.cn
public class DSApp extends BookApp {
    private static final String BASE_URL = "https://ibooks.org.cn";
    private static final String HOME = "HOME";
    private static final String ITEMS = "ITEMS";

    public static void main(String[] args) {
        DSApp app = new DSApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();
        TaskModel taskModel = new TaskModel(this, HOME);
        taskModel.url = BASE_URL;
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

        Elements pageEles = task.resDoc.select("#content > nav.navigation.pagination > div.nav-links > a.page-numbers");
        String lastPage = pageEles.get(pageEles.size() - 2).attr("href");
        String pageCount = lastPage.substring(lastPage.lastIndexOf("/") + 1);
        int count = Integer.parseInt(pageCount);

        scanInfoModel.cateInfo.put(DEFAULT_SCAN_CATE, new ScanInfoModel.ScanInfo(DEFAULT_SCAN_CATE, count));
        count = pageCountOff(count, DEFAULT_SCAN_CATE);

        for (int i = 1; i <= count; i++) {
            //https://ibooks.org.cn/page/98
            TaskModel taskModel = new TaskModel(this, LIST);
            taskModel.url = "https://ibooks.org.cn/page/" + i;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Document doc = task.resDoc;
        Elements urls = doc.select("header.entry-header > h1.entry-title > a");
        for (Element u : urls) {
            TaskModel taskModel = createTask(INFO);
            taskModel.url = u.attr("href");

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    //https://ibooks.org.cn/archives/7027#download-block
    private void parseInfo(TaskModel task) {
        Document doc = task.resDoc;
        Elements urls = doc.select("#download-block > a.button.download-button");
        Element nameEle = doc.selectFirst("#content > article > header.entry-header > h1.entry-title");
        String bookName = nameEle.text();

        InfoModel model = new InfoModel();
        model.bookName = bookName;
        model.pageUrl = task.url;
        DownModel downModel = new DownModel();
        if (!urls.isEmpty()) {
            downModel.setDownUrl(urls.get(0).attr("href"));
            String text = urls.get(0).text();
            if (text.contains("百度云盘下载")) {
                downModel.type = BookConstant.BAIDU_PAN;
                downModel.code = text.substring(text.lastIndexOf("(") + 1, text.lastIndexOf(")"));
            } else {
                D.w(String.format("desc:%s err:无法获取百度提取码  url:%s", task.desc, task.url));
            }
        }
        model.addDownModel(downModel);

        D.i("model==>" + model.toString());
        saveBook(model);
    }

}
