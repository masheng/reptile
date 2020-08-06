package com.books.zx;

import com.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//http://www.zxcs.me
public class ZXApp extends BookApp {
    private static final String CATEGORY = "category";
    private static final String ITEMS = "ITEMS";
    private static final String INFO = "INFO";
    private static final String DOWN_INFO = "DOWN_INFO";

    public static void main(String[] args) {
        ZXApp app = new ZXApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel taskModel = createTask(HOME);
        taskModel.url = "http://www.zxcs.me";
        addHttpTask(taskModel);
    }

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case HOME:
                parseHome(task);
                break;
            case CATEGORY:
                parseCategory(task);
                break;
            case LIST:
                parseList(task);
                break;
            case INFO:
                parseInfo(task);
                break;
            case DOWN_INFO:
                parseDownUrl(task);
                break;
        }
    }

    private void parseHome(TaskModel task) {
        Elements clses = task.resDoc.select("#topmenu > li.common > a");
        for (Element cls : clses) {
            String url = cls.attr("href");
            String cateInfo = cls.getElementsByTag("span").text();
            TaskModel taskModel = createTask(CATEGORY);
            taskModel.url = url;
            taskModel.desc = cateInfo;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    //遍历类目的所有页
    private void parseCategory(TaskModel task) {
        Document doc = task.resDoc;
        Elements item = doc.select("#pagenavi > a");
        int count;
        String url = item.last().attr("href");
        if ("尾页".equals(item.last().attr("title"))) {
            String tail = url.substring(url.lastIndexOf("/") + 1);
            count = Integer.parseInt(tail);
        } else {
            //页面是从第二页开始算的
            count = item.size();
        }

        String cateMd5 = MD5Utils.strToMD5(task.desc);
        scanInfoModel.cateInfo.put(cateMd5, new ScanInfoModel.ScanInfo(task.desc, count));
        count = pageCountOff(count, cateMd5);

        String baseUrl = url.substring(0, url.lastIndexOf("/") + 1);
        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = baseUrl + i;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    //解析每一页的数据
    private void parseList(TaskModel task) {
        Document doc = task.resDoc;
        Elements items = doc.select("#plist");

        for (Element item : items) {
            Element dt = item.selectFirst("dt > a");
            String itemUrl = dt.attr("href");

            if (BookDBUtls.testSaveSiteInfo(itemUrl)) {
                InfoModel infoModel = new InfoModel();
                infoModel.bookName = dt.text();
                infoModel.bookFormat = BookConstant.F_TXT;

                TaskModel taskModel = createTask(INFO);
                taskModel.url = itemUrl;
                infoModel.pageUrl = itemUrl;
                taskModel.infoModel = infoModel;
                addHttpTask(taskModel);
            }

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        Document doc = task.resDoc;
        Element downPage = doc.selectFirst("#content > div.pagefujian > div.filecont > p.filetit > a");

        String url = downPage.attr("href");

        task.clear();
        task.url = url;
        task.tag = DOWN_INFO;
        addHttpTask(task);
    }

    //http://www.zxcs.me/download.php?id=12021
    private void parseDownUrl(TaskModel task) {
        Document doc = task.resDoc;
        Elements downUrls = doc.select("body > div.wrap > div.content > div.panel > div.panel-body > span.downfile > a");
        for (int i = 0; i < downUrls.size(); i++) {
            String url = downUrls.get(i).attr("href");
            task.infoModel.addDownModel(new DownModel(url));
        }

        D.p("info==>" + task.infoModel.toString());

        saveBook(task.infoModel);
    }
}
