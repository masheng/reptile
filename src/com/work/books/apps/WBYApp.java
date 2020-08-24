package com.work.books.apps;

import com.work.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.sun.istack.internal.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//微百阅  http://www.weibaiyue.com/sheke/
public class WBYApp extends BookApp {
    private static final String BASE_URL = "http://www.weibaiyue.com";
    private static final String CTFILE = "https://089u.com/";

    public static void main(String[] args) {
        WBYApp app = new WBYApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel model = createTask(HOME);
        model.resEncode = TaskModel.GB2312;
        model.url = "http://www.weibaiyue.com/sheke/";
        addHttpTask(model);
    }

    @Override
    public void parse(@NotNull TaskModel task) {
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

    private void parseHome(TaskModel task) {
        Elements categorys = task.resDoc.select("body > div.nav > ul > li:not(:first-child) > a");
        for (Element cate : categorys) {
            TaskModel model = createTask(CATEGORY);
            model.resEncode = TaskModel.GB2312;
            model.url = BASE_URL + cate.attr("href");
            model.cate = cate.text();
            addHttpTask(model);

            if (D.DEBUG)
                break;
        }
    }

    private void parseCate(TaskModel task) {
        //获取的第一页直接解析 第一页与后续页面url不同
        parseList(task);

        //获取总页数 然后组装url
        int count = 0;
        try {
            Elements pageEle = task.resDoc.select("body > div.liebiaoye > div.liebiaoye-left > a:last-child");
            if (pageEle.isEmpty())
                return;

            String lastUrl = pageEle.get(0).attr("href");
            //<a href="/jiaoyu/index_34.html">尾页</a>
            String pageCount = lastUrl.substring(lastUrl.lastIndexOf('_') + 1, lastUrl.lastIndexOf('.'));
            count = Integer.parseInt(pageCount);
        } catch (Exception e) {
            D.e("==>" + task.toString());
            e.printStackTrace();
        }

        String cateMd5 = MD5Utils.strToMD5(task.cate);
        scanInfoModel.cateInfo.put(cateMd5, new ScanInfoModel.ScanInfo(task.cate, count));
        count = pageCountOff(count, cateMd5, task.url);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.resEncode = TaskModel.GB2312;
            taskModel.url = String.format("%sindex_%d.html", task.url, i);
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Elements items = task.resDoc.select("body > div.liebiaoye > div.liebiaoye-left > div.liebiaoye-left-content > div.liebiaoye-left-content-info > div.liebiaoye-left-content-title > h2 > a");
        for (Element item : items) {
            TaskModel taskModel = createTask(INFO);
            taskModel.resEncode = TaskModel.GB2312;
            taskModel.url = BASE_URL + item.attr("href");

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        //body > div.liebiaoye > div.nengrongye-left > div > div:nth-child(10) > a
        Elements downELes = task.resDoc.select("body > div.liebiaoye > div.nengrongye-left > div.nengrongye-left-content > div.xiazai > a");
        String downUrl = null;
        for (Element down : downELes) {
            if (down.text().startsWith("PDF")) {
                downUrl = down.attr("href");
                break;
            }
        }

        Element bookNameEle = task.resDoc.selectFirst("body > div.liebiaoye > div.nengrongye-left > div > div.nengrongye-left-content-info > div.nengrongye-left-content-info-title > h1");
        String bookName = bookNameEle.text();

        InfoModel model = new InfoModel();
        model.pageUrl = task.url;
        if (bookName.contains("PDF"))
            model.bookFormat = BookConstant.F_PDF;

        model.bookName = bookName;
        //<h1>《芬兰人的噩梦》[PDF][TXT]电子书下载</h1>
        if (bookName.contains("《")) {
            model.bookName = StrUtils.subStr(bookName, "《", "》", true);
        }

        model.addDownModel(new DownModel(downUrl, downUrl.startsWith(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

        if (D.DEBUG)
            D.i("微百阅==>" + model.toString());
        saveBook(model);
    }
}
