package com.work.books.apps;

import com.work.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;

public class jb51App extends BookApp {
    private static final String BASE_URL = "https://www.jb51.net";

    private static final String HOME_URL = "https://www.jb51.net/do/book_class.html";

    @Override
    public void config() {
        super.config();
        TaskModel task = createTask(HOME);
        task.url = HOME_URL;
        task.resEncode = TaskModel.GB2312;
        addHttpTask(task);
    }

    @Override
    public boolean check(TaskModel task) {
        return true;
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
            case ITEM:
                parseItem(task);
                break;
            case INFO:
                parseInfo(task);
                break;
        }

//        D.i("parse==>" + task.response);
    }

    public static void main(String[] args) {
        jb51App jb = new jb51App();
        jb.start();
    }

    private void parseHome(TaskModel taskModel) {
        Document doc = taskModel.resDoc;
        Elements cateELes = doc.select("#mainBody > div.bgf.clearfix > div.box-software > dl.fix > dd > a");
        for (Element a : cateELes) {
            TaskModel task = createTask(LIST);
            task.url = BASE_URL + a.attr("href");
            task.resEncode = TaskModel.GB2312;
            addHttpTask(task);

            if (D.DEBUG)
                break;
        }
    }

    //https://www.jb51.net/books/list481_1.html
    private void parseList(TaskModel taskModel) {
        parseItem(taskModel);

        //获取书籍类别
        String cate = DEFAULT_SCAN_CATE;
        Element cateEle = taskModel.resDoc.selectFirst("#article > div.mtb10.lists-main > div.introduces > div > h1");
        if (cateEle != null) {
            cate = cateEle.text();
        }

        String baseUrl = taskModel.url.substring(0, taskModel.url.lastIndexOf(".html"));
        //解析页码
        Elements pageEless = taskModel.resDoc.select("div.dxypage > div.plist > a:last-child");
        if (pageEless.isEmpty()) {
            return;
        }
        String lastPageUrl = pageEless.get(pageEless.size() - 1).attr("href");
        String pageCount = lastPageUrl.substring(lastPageUrl.lastIndexOf("_") + 1, lastPageUrl.lastIndexOf(".html"));
        int count = Integer.parseInt(pageCount);

        String cateKey = MD5Utils.strToMD5(cate);
        scanInfoModel.cateInfo.put(cateKey, new ScanInfoModel.ScanInfo(cate, count));
        count = pageCountOff(count, cateKey, taskModel.url);

        for (int i = 2; i <= count; i++) {
            TaskModel task = createTask(ITEM);
            task.url = baseUrl + i + ".html";
            task.resEncode = TaskModel.GB2312;
            addHttpTask(task);

            if (D.DEBUG)
                break;
        }
    }

    private void parseItem(TaskModel task) {
        Elements urlEles = task.resDoc.select("#list_ul_more > li > div.top-tit > p > a");

        int index = 0;
        for (Element urlE : urlEles) {
            String url = BASE_URL + "/" + urlE.attr("href");
            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG && index++ == 2)
                break;
        }
    }

    private void parseInfo(TaskModel taskModel) {
        Document doc = taskModel.resDoc;
        InfoModel model = new InfoModel();
        model.pageUrl = taskModel.url;
        //获取书名
        Element name = doc.getElementById("soft-name");
        String bookName = "";
        if (name == null)
            return;
        else
            bookName = name.getElementsByTag("h1").get(0).text();
        try {
            model.bookName = new String(bookName.getBytes("GBK"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //类别
        Elements infos = doc.select("#param-content li.r");
        for (Element info : infos) {
            if (info.text().contains("书籍类别")) {
                model.bookType = info.getElementsByTag("span").text();
                break;
            }

            if (info.text().contains("应用平台")) {
                model.bookFormat = info.getElementsByTag("span").text();
                break;
            }
        }

        //下载地址
        Elements downloads = taskModel.resDoc.select("#download > ul > li.address-wrap.on > ul.ul_Address > li");

        boolean baiduWangPan = true;
        for (Element dl : downloads)
            if (!dl.hasClass("baidu")) {
                baiduWangPan = false;
                break;
            }

        int urlType = BookConstant.PRIVATE_PAN;
        String code = "";
        if (baiduWangPan) {
            urlType = BookConstant.BAIDU_PAN;
            code = taskModel.url.substring(taskModel.url.lastIndexOf("/") + 1, taskModel.url.lastIndexOf("."));
        }

        model.downModel = new HashSet<>();
        for (int i = 0; i < downloads.size(); i++) {
            String url = downloads.get(i).getElementsByTag("a").attr("href");
            DownModel downloadUrl = new DownModel(url, code, urlType);
            model.downModel.add(downloadUrl);
        }

        if (D.DEBUG)
            D.i("==>" + model);

        saveBook(model);
    }
}
