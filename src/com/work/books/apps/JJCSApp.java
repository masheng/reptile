package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.HttpUtils;
import com.work.books.utils.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//https://jingjiaocangshu.cn
public class JJCSApp extends BookApp {
    private static final String CTFILE = "https://u062.com";

    public static void main(String[] args) {
        JJCSApp app = new JJCSApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();
        TaskModel task = createTask(HOME);
        task.url = "https://jingjiaocangshu.cn";
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
            case DOWN:
                parseDown(task);
                break;
        }
    }

    private void parseHome(TaskModel task) {
        //depth-0 has_children b2-menu-3
        Elements cateEles = task.resDoc.select("#top-menu-ul > li:not(:first-child)");
        for (Element ele : cateEles) {
            if (ele.hasClass("has_children")) {
                Elements eleItems = ele.select("ul.sub-menu > li");
                for (Element e : eleItems)
                    createCate(e);
            } else {
                createCate(ele);
            }

            if (D.DEBUG)
                break;
        }
    }

    private boolean createCate(Element ele) {
        String cateUrl = ele.getElementsByTag("a").attr("href");
        if (StrUtils.isEmpty(cateUrl) || !cateUrl.contains("http"))
            return false;

        TaskModel taskModel = createTask(CATEGORY);
        taskModel.url = cateUrl;

        Elements eleTags = ele.getElementsByTag("span");
        for (Element e : eleTags)
            if (!StrUtils.isEmpty(e.text())) {
                taskModel.cate = e.text();
                break;
            }
//        D.i(String.format("%s %s", taskModel.cate, taskModel.url));
        addHttpTask(taskModel);
        return true;
    }

    private void parseCate(TaskModel task) {
        parseList(task);

        Element lastPage = task.resDoc.selectFirst("#primary-home > div.b2-pagenav.post-nav > div > div > div.btn-group > a:nth-last-child(2)");
        if (lastPage == null && (task.cate.contains("耽美小说") || task.cate.contains("PDF电子书")))
            return;

        String pageCount = lastPage.text();
        int count = Integer.parseInt(pageCount);

        String cateMd5 = MD5Utils.strToMD5(task.cate);
        scanInfoModel.cateInfo.put(cateMd5, new ScanInfoModel.ScanInfo(task.cate, count));
        count = pageCountOff(count, cateMd5, task.url);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = task.url + "/page/" + i;
            taskModel.cate = task.cate;
//            D.i("==>" + taskModel.url);
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Elements elePages = task.resDoc.select("#post-list > ul.b2_gap  > li > div > div.post-module-thumb > a");
        for (Element e : elePages) {
            String infoUrl = e.attr("href");
            if (!BookDBUtls.testSaveSiteInfo(infoUrl))
                continue;

            TaskModel taskModel = createTask(INFO);
            taskModel.url = infoUrl;
            taskModel.cate = task.cate;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        Element urlEle = task.resDoc.selectFirst("#primary-home > article > div.entry-content > div.edl_post_preview > div > a");
        String url = urlEle.attr("href");
        TaskModel taskModel = createTask(DOWN);
        taskModel.url = url;
        taskModel.cate = task.cate;
        addHttpTask(taskModel);
    }

    private void parseDown(TaskModel task) {
        Element titleEle = task.resDoc.selectFirst("body > div.wrap > div.content > h2");
        String title = titleEle.text();
        InfoModel infoModel = new InfoModel();
        infoModel.pageUrl = task.url;
        infoModel.bookType = task.cate;
        //<h2>《楚楚（出书版）》作者：轩辕悬</h2>
        infoModel.bookName = StrUtils.subStr(title, "《", "》", true);
        if (title.contains("作者"))
            infoModel.bookAuthor = title.split("：")[1];
        //解析格式
        Elements infoEles = task.resDoc.select("body > div.wrap > div.content > div > div.plus_l > ul > li");
        for (Element info : infoEles) {
            //<li>小说格式 ：TXT</li>
            if (info.text().contains("小说格式")) {
                infoModel.bookFormat = info.text().split("：")[1];
                continue;
            }
            //<strong style="color:red;font-weight:bold;font-size:18px;">解压密码 ：jjcs</strong>
            if (info.text().contains("解压密码")) {
                infoModel.password = info.text().split("：")[1];
            }
        }
        //解析下载地址
        Elements downEles = task.resDoc.select("body > div.wrap > div.content > div.plus_box > div.panel > div.panel-body > span.downfile > a");
        for (Element e : downEles)
            infoModel.addDownModel(new DownModel(e.text(), e.text().startsWith(CTFILE)
                    ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

        D.i("==>" + infoModel.toString());

        saveBook(infoModel);
    }

}
