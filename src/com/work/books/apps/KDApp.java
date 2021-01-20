package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.BookConstant;
import com.work.books.utils.DownModel;
import com.work.books.utils.InfoModel;
import com.work.books.utils.StrUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KDApp extends BookAppTemp {
    public static void main(String[] args) {
        KDApp app = new KDApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel task = createTask(HOME);
        task.url = "https://kindleer.com";
        addHttpTask(task);
    }

    @Override
    protected void parseHome(TaskModel task) {
        parseList(task);

        Element pageCountEle = task.resDoc.selectFirst("#primary > nav > div > a:nth-last-child(2)");
        String pageCountUrl = pageCountEle.attr("href");
        String pageCount = StrUtils.subStr(pageCountUrl, "page/", "/", false);
        int count = Integer.parseInt(pageCount);

        count = cateCountDefault(task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = "https://kindleer.com/page/" + i + "/";
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {
        Elements listEles = task.resDoc.select("#main > article:not(.category-zixun) > header > h2 > a");
        for (Element e : listEles) {
            InfoModel infoModel = new InfoModel();

            String url = e.attr("href");
            String title = e.text();
            parseName(infoModel, title);
            //组装详情页
            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            infoModel.pageUrl = url;
            taskModel.infoModel = infoModel;
            taskModel.testSite = true;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseInfo(TaskModel task) {
        Elements downEles = task.resDoc.select("#main > article > div > div.single-content > p");
        Element downEle = null;
        for (int i = downEles.size() - 1; i >= 0; i--) {
            Element tmp = downEles.get(i).selectFirst("a");
            if (tmp != null) {
                downEle = downEles.get(i);
                break;
            }
        }
        if (downEle == null) {
            D.e("KDApp parseInfo 获取url失败==>" + task.url);
            return;
        }
        String url = downEle.selectFirst("a").attr("href");
        //<a href="https://kindleer.com/wp-content/themes/Begin/inc/go.php?url=http://pan.baidu.com/s/1pL8RJSJ" site="mebook" target="_blank" rel="noopener noreferrer">百度网盘</a>

        String codeStr = downEle.text();
        String[] downUrl = null;
        if (codeStr.contains("提取码："))
            downUrl = codeStr.split("提取码：");
        else if (codeStr.contains("提取码:"))
            downUrl = codeStr.split("提取码:");

        String code = "";
        if (downUrl != null && downUrl.length > 1)
            code = downUrl[1].trim();

        if (StrUtils.isEmpty(url)) {
            D.e("获取下载地址失败==>" + task.url);
            return;
        }

        task.infoModel.addDownModel(new DownModel(url, code, BookConstant.PRIVATE_PAN));

        TaskModel taskModel = createTask(DOWN);
        taskModel.url = url;
        taskModel.infoModel = task.infoModel;
        addHttpTask(taskModel);
    }

    @Override
    protected void parseDown(TaskModel task) {
        Element downEle = task.resDoc.selectFirst("#main > div > div > div.alert-concent > div.alert-url");
        String realUrl = downEle.text();
        if (task.infoModel.downModel.size() != 1)
            throw new RuntimeException("下载地址有多个 需要处理");

        for (DownModel down : task.infoModel.downModel) {
            down.setDownUrl(realUrl);
            down.type = BookConstant.isBaiduPan(realUrl) ? BookConstant.BAIDU_PAN : BookConstant.PRIVATE_PAN;
        }

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
