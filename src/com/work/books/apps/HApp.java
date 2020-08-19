package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.HttpUtils;
import com.work.books.utils.BookConstant;
import com.work.books.utils.DownModel;
import com.work.books.utils.InfoModel;
import com.work.books.utils.StrUtils;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class HApp extends BookAppTemp {
    private static final String HOST = "https://www.booksgather.com";
    private static final String DOWN1 = "down1";
    private Map<String, String> headers = new HashMap<>();

    public static void main(String[] args) {
        HApp app = new HApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();
        TaskModel task = createTask(HOME);
        task.url = HOST;
        addHttpTask(task);
//
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("devuid", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjcmVhdGVUaW1lIjoxNTk3ODI3NjY5LCJpcCI6IjEyNC4yMDcuMjYuNjkiLCJ1c2VyQWdlbnQiOiJNb3ppbGxhLzUuMCAoTWFjaW50b3NoOyBJbnRlbCBNYWMgT1MgWCAxMF8xNV8yKSBBcHBsZVdlYktpdC82MDUuMS4xNSAoS0hUTUwsIGxpa2UgR2Vja28pIFZlcnNpb24vMTMuMC40IFNhZmFyaS82MDUuMS4xNSJ9.PJwIpINwEYj_VYupwE6YBEfpSVmfoqiw3Ai4hN8-yn4");
        headers.put("accept-encoding", "gzip, deflate");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headers.put("accept-language", "zh-cn");
    }

    @Override
    public void parse(TaskModel task) {
        super.parse(task);
        if (task.tag.equals(DOWN1))
            parseDown1(task);
    }

    @Override
    protected void parseHome(TaskModel task) {
        Elements cateEles = task.resDoc.select("#__layout > div > div:nth-child(1) > header.header-top-desktop > div > ul > li:not(:first-child) > a");
        for (Element e : cateEles) {
            TaskModel taskModel = createTask(CATEGORY);
            taskModel.url = HOST + e.attr("href");
            taskModel.cate = e.text();
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseCate(TaskModel task) {
        parseList(task);

        Element countPageEle = task.resDoc.selectFirst("#__layout > div > div.container.body-content > div.category-wrapper.expand-body.content-wrapper > div > div.pagination-wrapper > ul > li.page-item.active > a");
        String countStr = countPageEle.attr("aria-setsize");
        int count = Integer.parseInt(countStr);

        count = cateCount(task.cate, task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.cate = task.cate;
            taskModel.url = task.url + "?pageNumber=" + i;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {
        Elements listEles = task.resDoc.select("#__layout > div > div.container.body-content > div.category-wrapper.expand-body.content-wrapper > div > div.books.row > div > div > div.book > div.book-info > h6 > a");
        for (Element e : listEles) {
            TaskModel taskModel = createTask(INFO);
            taskModel.url = HOST + e.attr("href");
            taskModel.cate = task.cate;
            taskModel.testSite = true;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseInfo(TaskModel task) {
        InfoModel model = new InfoModel();
        Element bookNameEle = task.resDoc.selectFirst("#__layout > div > div.container.body-content > div.book-detail-wrapper.content-wrapper.expand-body > div > h5");
        String bookName = bookNameEle.text();
        parseName(model, bookName);

        Element bookImgEle = task.resDoc.selectFirst("#__layout > div > div.container.body-content > div.book-detail-wrapper.content-wrapper.expand-body > div > img");
        model.bookImg = bookImgEle.attr("src");

        Element bookDescEle = task.resDoc.selectFirst("#__layout > div > div.container.body-content > div.book-detail-wrapper.content-wrapper.expand-body > div > div.summary");
        model.bookDesc = bookDescEle.text();
        model.bookType = task.cate;
        model.pageUrl = task.url;

        TaskModel taskModel = createTask(DOWN);
        taskModel.infoModel = model;
        String code = task.url.substring(task.url.lastIndexOf("/") + 1);
        taskModel.url = HOST + "/api/book/passcode/" + code;
        if (headers.containsKey("referer"))
            headers.remove("referer");
        taskModel.headers = headers;
        taskModel.requestType = HttpUtils.GET;
        taskModel.obj = code;
        taskModel.parse = false;
        addHttpTask(taskModel);
    }

    @Override
    protected void parseDown(TaskModel task) {
        JSONObject obj = new JSONObject(task.response);
        JSONObject data = obj.getJSONObject("data");
        String passcode = data.getString("passcode");
//:path	/api/book/download/bmtfnub24te0g2326o00/a0d06cb5
        headers.put("referer", task.url);
        TaskModel taskModel = createTask(DOWN1);
        taskModel.headers = headers;
        taskModel.requestType = HttpUtils.GET;
        taskModel.infoModel = task.infoModel;
        taskModel.parse = false;
        taskModel.url = String.format("%s/api/book/download/%s/%s", HOST, task.obj, passcode);
        addHttpTask(taskModel);
    }

    private void parseDown1(TaskModel task) {
        JSONObject obj = new JSONObject(task.response);
        JSONObject data = obj.getJSONObject("data");
        JSONObject book = data.getJSONObject("book");

        String BaidupanLink = book.getString("BaidupanLink");
        String BaidupanPassword = book.getString("BaidupanPassword");
        if (BaidupanPassword.contains("api"))
            BaidupanPassword = HOST + BaidupanPassword;
        String Cloud189 = book.getString("Cloud189");
        String Cloud189password = book.getString("Cloud189password");

        task.infoModel.addDownModel(new DownModel(BaidupanLink, BaidupanPassword, BookConstant.BAIDU_PAN));
        task.infoModel.addDownModel(new DownModel(Cloud189, Cloud189password, BookConstant.TY_PAN));

        if (D.DEBUG)
            D.i("==>" + task.infoModel.toString());

        saveBook(task.infoModel);
    }

    private void parseName(InfoModel infoModel, String title) {
        title = title.trim();
        if (title.contains("《")) {
            infoModel.bookName = StrUtils.subStr(title, "《", "》", true);
            if (title.contains("（作者）")) {
                String[] author = title.split("（作者）");
                infoModel.bookAuthor = author[0].substring(author[0].indexOf("》") + 1);
            } else if (title.contains("+")) {
                String author = title.substring(title.indexOf("》") + 1).toLowerCase();
                String[] splie = author.split("\\+");
                String f = endFormat(splie[0]);
                if (!StrUtils.isEmpty(f))
                    infoModel.bookAuthor = splie[0].substring(0, splie[0].indexOf(f));
                else
                    infoModel.bookAuthor = splie[0];
            }

            if (StrUtils.isEmpty(infoModel.bookAuthor) && title.lastIndexOf("》") + 1 != title.length()) {
                String format = endFormat(title);
                if (StrUtils.isEmpty(format)) {
                    infoModel.bookAuthor = title.split("》")[1];
                } else {
                    infoModel.bookAuthor = title.substring(title.indexOf("》") + 1, title.toLowerCase().indexOf(format)).trim();
                }
            }
        } else if (title.contains("-")) {
            String name[] = title.split("-");
            infoModel.bookName = name[0];
            infoModel.bookAuthor = name[1];
        }

        //解析格式
        StringBuilder formatStr = new StringBuilder();
        if (title.contains("+")) {
            String[] format = title.split("\\+");
            format[0] = endFormat(format[0]);
            for (int i = 0; i < format.length; i++) {
                if (!StrUtils.isEmpty(format[i])) {
                    formatStr.append(format[i]);
                    if (i != format.length - 1)
                        formatStr.append("/");
                }
            }

            infoModel.bookFormat = formatStr.toString();
        }
        if (StrUtils.isEmpty(infoModel.bookFormat))
            infoModel.bookFormat = endFormat(title);
    }

    private String endFormat(String name) {
        if (name.toLowerCase().contains(BookConstant.F_EPUB))
            return BookConstant.F_EPUB;
        else if (name.toLowerCase().contains(BookConstant.F_MOBI))
            return BookConstant.F_MOBI;
        else if (name.toLowerCase().contains(BookConstant.F_AZW3))
            return BookConstant.F_AZW3;
        return "";
    }
}
