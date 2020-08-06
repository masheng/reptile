package com.work.books.apps;

import com.work.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//https://blah.me
public class BlahApp extends BookApp {
    private static final String BASE_URL = "https://blah.me";

    public static void main(String[] args) {
        BlahApp app = new BlahApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();
        TaskModel taskModel = createTask(HOME);
        taskModel.url = BASE_URL;
        addHttpTask(taskModel);
    }

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case HOME:
                parseHome(task);
                break;
            case INFO:
                parseInfo(task);
                break;
        }
    }

    //https://blah.me/book/2247
    //https://blah.me/download/ebook/2294/epub
    private void parseInfo(TaskModel task) {
        Elements bookEles = task.resDoc.select("div.ok-book-download > a.okBookDownload");
        for (Element book : bookEles) {
            String name = book.attr("data-book-title");
            String format = book.attr("data-book-type");
            String downUrl = book.attr("href");
            String pageIndex = StrUtils.subStr(downUrl, "ebook/", "/", false);

            InfoModel model = new InfoModel();
            model.pageUrl = String.format("https://blah.me/read/%s/#/", pageIndex);
            model.bookName = name;
            model.bookFormat = format;
            model.addDownModel(new DownModel(BASE_URL + downUrl));
//            D.i("blah==>" + model.toString());

            saveBook(model);
        }
    }

    private void parseHome(TaskModel task) {
        Elements pages = task.resDoc.select("#okContentWrap > ul > li:last-child > a");
        if (pages.isEmpty()) {
            D.e("==>Blah网站错误 获取总页数为0");
            return;
        }

        String pageCount = pages.get(0).attr("data-page");
        int count = Integer.parseInt(pageCount);

        scanInfoModel.cateInfo.put(DEFAULT_SCAN_CATE, new ScanInfoModel.ScanInfo(DEFAULT_SCAN_CATE, count));
        count = pageCountOff(count, DEFAULT_SCAN_CATE);

        for (int i = 1; i <= count; i++) {
            TaskModel taskModel = createTask(INFO);
            taskModel.url = String.format("%s/?p=%d", BASE_URL, i);

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }
}
