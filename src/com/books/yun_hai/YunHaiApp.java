package com.books.yun_hai;

import com.books.utils.*;
import com.company.core.App;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//http://www.pdfbook.cn
public class YunHaiApp extends BookApp {
    private static final String CTFILE = "https://n459.com/";

    public static void main(String[] args) {
        YunHaiApp app = new YunHaiApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel task = createTask(HOME);
        task.url = "http://www.pdfbook.cn";
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
        Elements cateEles = task.resDoc.select("#NavBlock > div > ul > li:not(:first-child) > a");
        for (Element cate : cateEles) {
            String path = cate.attr("href");
            String cateInfo = cate.text();

            TaskModel taskModel = createTask(CATEGORY);
            taskModel.url = path;
            taskModel.desc = cateInfo;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseCate(TaskModel task) {
        parseList(task);

        String pageEle = task.resDoc.selectFirst("#pagenavi > span.pages").text();
        String pageCount = pageEle.substring(pageEle.lastIndexOf("共") + 1, pageEle.lastIndexOf("页"));
        int count = Integer.parseInt(pageCount);

        String cateMd5 = MD5Utils.strToMD5(task.desc);
        scanInfoModel.cateInfo.put(cateMd5, new ScanInfoModel.ScanInfo(task.desc, count));
        count = pageCountOff(count, cateMd5);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = task.url + "/page/" + i;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Elements list = task.resDoc.select("#main > ul.image_box > li > h1 > a");
        for (Element item : list) {
            String url = item.attr("href");
            String bookName = item.attr("title");

            InfoModel infoModel = new InfoModel();
            infoModel.bookName = bookName;

            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            taskModel.infoModel = infoModel;

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        Elements downEles = task.resDoc.select("#main > div.post > div > p");

        task.infoModel.pageUrl = task.url;

        for (int i = downEles.size() - 1; i >= 0; i--) {
            if (downEles.get(i).text().contains("点击下载")) {
                Element downInfoEle = downEles.get(i).selectFirst("span > a");
                String downUrl = downInfoEle.attr("href");
                task.infoModel.addDownModel(new DownModel(downUrl, downUrl.startsWith(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

                String name = downInfoEle.text();
                if (name.contains("PDF"))
                    task.infoModel.bookFormat = BookConstant.F_PDF;
//<a style="color: #993300;" href="https://n459.com/file/1163421-401825158" target="_blank">《威科夫操盘法 华尔街大师成功驾驭市场超过95年的秘技》(PDF扫描版/124.34M)</a>
                task.infoModel.bookName = name;
                if (name.contains("《"))
                    task.infoModel.bookName = StrUtils.subStr(name, "《", "》", true);

                break;
            }
        }

        D.p("云海==>" + task.infoModel.toString());

        saveBook(task.infoModel);
    }

}
