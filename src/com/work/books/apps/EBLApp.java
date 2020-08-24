package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.HttpUtils;
import com.work.books.utils.BookConstant;
import com.work.books.utils.DownModel;
import com.work.books.utils.InfoModel;
import com.work.books.utils.StrUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class EBLApp extends BookAppTemp {
    private static final String CTFILE = "https://545c.com";
    private Map<String, String> headers = new HashMap<>();

    public static void main(String[] args) {
        EBLApp app = new EBLApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headers.put("Accept-Language", "zh-cn");
        headers.put("Accept-Encoding", "gzip, deflate");

        TaskModel task = createTask(HOME);
        task.url = "https://ebooklist.mobi";
        addHttpTask(task);
    }

    @Override
    protected void parseHome(TaskModel task) {
        Elements cateEles = task.resDoc.select("body > header > div.container > ul.site-nav > li.menu-item-object-category > a");
        for (Element e : cateEles) {
            TaskModel taskModel = createTask(CATEGORY);
            taskModel.cate = e.text();
            taskModel.url = e.attr("href");
            taskModel.headers = headers;
            taskModel.requestType = HttpUtils.GET;
            taskModel.delayTime = 500;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseCate(TaskModel task) {
        parseList(task);

        Element pageCountEle = task.resDoc.selectFirst("body > section > div.content-wrap > div > div.pagination > ul > li:nth-last-child(1) > span");
        String pageCount = pageCountEle.text();
        String[] pageStr = pageCount.split(" ");
        int count = Integer.parseInt(pageStr[1]);

        count = cateCount(task.cate, task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.cate = task.cate;
            taskModel.url = String.format("%s/page/%d", task.url, i);
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {
        Elements listEles = task.resDoc.select("body > section > div.content-wrap > div > article > header > h2 > a");
        for (Element e : listEles) {
            String url = e.attr("href");
            String title = e.text();

            InfoModel infoModel = new InfoModel();
            infoModel.pageUrl = url;
            infoModel.bookType = task.cate;

            String[] name = testFormat(title.toLowerCase());
            if (StrUtils.isEmpty(name[1]))
                infoModel.bookName = title;
            else
                infoModel.bookName = title.substring(0, title.toLowerCase().indexOf(name[1]));
//<a href="https://ebooklist.mobi/2019/09/24/28814.html">C#初学者指南 – [加拿大]Jayden Ky Azw3下载</a>
//<a href="https://ebooklist.mobi/2019/09/29/29322.html">失语者 – [美]马丁·皮斯托留斯 Azw3下载</a>
            if (infoModel.bookName.contains("–")) {
                String[] book = infoModel.bookName.split("–");
                infoModel.bookName = book[0];
                infoModel.bookAuthor = book[1];
            }


            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            taskModel.infoModel = infoModel;
            taskModel.testSite = true;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseInfo(TaskModel task) {
        Elements downEles = task.resDoc.select("body > section > div.content-wrap > div > article > div.blog-single-content.bordered.blog-container > table > tbody > tr > td:nth-child(1) > a");
        for (int i = 0; i < downEles.size(); i++) {
            InfoModel infoModel = (i == 0) ? task.infoModel : task.infoModel.clone();
            String urlStr = downEles.get(i).attr("href");
            String title = downEles.get(i).text();
            infoModel.bookFormat = title.substring(title.lastIndexOf(".") + 1).toLowerCase();
            //<a href="https://ebooklist.mobi/go/?url=https://ebookbug.ctfile.com/fs/9104807-386606994" target="_blank" rel="noopener noreferrer"><br>
            String url = urlStr.substring(urlStr.indexOf("url=") + "url=".length());

            if (url.contains("ctfile")) {
                TaskModel taskModel = createTask(DOWN);
                taskModel.url = url;
                taskModel.redirect = true;
                taskModel.infoModel = infoModel;
                addHttpTask(taskModel);
            } else {
                infoModel.addDownModel(new DownModel(url, url.contains(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

                D.i("==>" + infoModel.toString());
                saveBook(infoModel);
            }
        }
    }

    @Override
    protected void parseDown(TaskModel task) {
        if (task.redirect) {
            task.infoModel.addDownModel(new DownModel(task.redirectUrl, task.redirectUrl.contains(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));
            if (D.DEBUG)
                D.i("==>" + task.infoModel.toString());
            saveBook(task.infoModel);
        } else {
            D.e("保存下载地址失败 ==>" + task.infoModel.toString());
        }
    }
}
