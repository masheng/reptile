package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;

public class MNApp extends BookApp {
    public static void main(String[] args) {
        MNApp app = new MNApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();
        TaskModel task = createTask(HOME);
        task.url = "https://book.mzh.ren";
        addHttpTask(task);
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

    private void parseHome(TaskModel task) {
        Elements cateEles = task.resDoc.select("#menu-main > li > a");
        for (Element e : cateEles) {
            String cate = e.text();
            if (cate.contains("首页") || cate.contains("娱乐") || cate.contains("关于"))
                continue;

            TaskModel taskModel = createTask(CATEGORY);
            taskModel.url = e.attr("href");
            taskModel.cate = cate;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseCate(TaskModel task) {
        parseList(task);

        Element pageCountEle = task.resDoc.selectFirst("#main > nav > div > a:nth-last-child(2)");
        int count = Integer.parseInt(pageCountEle.text());

        String cateMd5 = MD5Utils.strToMD5(task.cate);
        scanInfoModel.cateInfo.put(cateMd5, new ScanInfoModel.ScanInfo(task.cate, count));
        count = pageCountOff(count, cateMd5, task.url);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = task.url + "/page/" + i;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Elements listEles = task.resDoc.select("#post-wrapper > div > article > header.entry-header > h2 > a");
        for (Element e : listEles) {
            String bookName = e.text();
            String url = e.attr("href");

            InfoModel infoModel = new InfoModel();
            infoModel.bookName = bookName;

            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            taskModel.testSite = true;
            infoModel.pageUrl = taskModel.url;
            taskModel.infoModel = infoModel;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    //https://book.mzh.ren/deeplearning-and-tensorflow-books.html
    //https://book.mzh.ren/sql-basic-tutorial.html
    //https://book.mzh.ren/how-to-develop-a-search-engine.html
    private void parseInfo(TaskModel task) {
        Element downEle = task.resDoc.selectFirst("#main > article > div > div.download-link");
        Elements urlEles = downEle.getElementsByTag("a");
        if (!urlEles.isEmpty()) {
            for (Element e : urlEles)
                task.infoModel.addDownModel(new DownModel(e.attr("href")));
        } else if (!StrUtils.isEmpty(downEle.text())) {
            if (downEle.text().contains("密码") || downEle.text().contains("提取码")) {
                String downUrl = downEle.text();
                if (downUrl.contains("链接："))
                    downUrl = downUrl.substring("链接：".length());

                if (downUrl.contains("密码:")) {
                    savePan(downUrl.split("密码:"), task);
                    //书籍下载&在线阅读 https://pan.baidu.com/s/1y5iq9Akj2a2e3-XFaALgcg 提取码：kklp 微信关注“码中人”公众号，获取免费赠书。 本站的大部分电子书均为开源电子书。 本站不制作 、不存储该资源，所有资源来自于其它网站。 如本电子书非开源图书，请尊重版权，购买正版书籍 本电子版仅供预览，下载后24小时内务必删除。 PS:如果链接失效，请留言告知我们，将尽快修复链接。
                } else if (downUrl.contains("提取码：")) {
                    savePan(downUrl.split("提取码："), task);
                } else if (downUrl.contains("提取码:"))
                    savePan(downUrl.split("提取码:"), task);
                else if (downUrl.contains("密码："))
                    savePan(downUrl.split("密码："), task);
            } else if (downEle.text().contains("公众号")) {
                return;
            } else {
                D.e("获取下载地址失败==>" + task.url);
                return;
            }
        } else {
            D.e("获取下载地址失败==>" + task.url);
            return;
        }

        if (!task.infoModel.downModel.isEmpty())
            saveBook(task.infoModel);
        else
            D.e("保存数据失败 没有下载地址==>" + task.url);
    }

    private void savePan(String[] url, TaskModel task) {
        String urlStr = url[0];
        if (!url[0].startsWith("http")) {
            urlStr = urlStr.substring(url[0].lastIndexOf("http")).trim();
        }
        String code = url[1];
        if (url[1].length() > 7) {
            String[] codes = url[1].split(" ");
            for (String s : codes)
                if (!StrUtils.isEmpty(s)) {
                    code = s;
                    break;
                }
        }
        if (StrUtils.isEmpty(code)) {
            D.ee("==>" + Arrays.toString(url));
        }

        if (url[0].contains("share.weiyun")) {
            task.infoModel.addDownModel(new DownModel(urlStr, code, BookConstant.TX_PAN));
        } else if (url[0].contains("pan.baidu")) {
            task.infoModel.addDownModel(new DownModel(urlStr, code, BookConstant.BAIDU_PAN));
        }
    }
}
