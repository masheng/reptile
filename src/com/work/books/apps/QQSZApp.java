package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.BookConstant;
import com.work.books.utils.DownModel;
import com.work.books.utils.InfoModel;
import com.work.books.utils.StrUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class QQSZApp extends BookAppTemp {
    private Map<String, String> cateUrls = new HashMap<>();
    private static final String CTFILE1 = "https://t00y.com";
    private static final String CTFILE2 = "https://sn9.us";

    public static void main(String[] args) {
        QQSZApp app = new QQSZApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        cateUrls.put("网络小说", "https://www.qqszz.com/wangluokk");
        cateUrls.put("畅销图书", "https://www.qqszz.com/changxiukk");
        cateUrls.put("经典合集", "https://www.qqszz.com/tushuheji");
        cateUrls.put("矛盾文学奖", "https://www.qqszz.com/maodunwx");
        cateUrls.put("诺贝尔文学奖", "https://www.qqszz.com/nbewx");
        cateUrls.put("教材辅导", "https://www.qqszz.com/jiaocai");

        for (Map.Entry<String, String> entry : cateUrls.entrySet()) {
            TaskModel task = createTask(CATEGORY);
            task.url = entry.getValue();
            task.cate = entry.getKey();
            addHttpTask(task);
        }
    }

    @Override
    protected void parseHome(TaskModel task) {

    }

    @Override
    protected void parseCate(TaskModel task) {
        parseList(task);

        int count = 1;
        Element pageCountEle = task.resDoc.selectFirst("#main-content > div.container-fluid > div > section > div > div > div.page-nav > span.pages");
        if (pageCountEle != null) {
            String pageCountStr = pageCountEle.text();
            String pageCount = pageCountStr.split("of")[1].trim();
            count = Integer.parseInt(pageCount);
        }

        count = cateCount(task.cate, task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = task.url + "/page/" + i;
            taskModel.cate = task.cate;
            taskModel.delayTime = 1500;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {
        Elements listEles = task.resDoc.select("#main-content > div.container-fluid > div > section > div > div > ul > li > article > h2 > a");
        for (Element e : listEles) {
            String url = e.attr("href");
            String title = e.text();

            InfoModel infoModel = new InfoModel();

            String[] formats = testFormat(title);
            try {
                if (StrUtils.isEmpty(formats[1]))
                    infoModel.bookName = title.trim();
                else
                    infoModel.bookName = title.substring(0, title.toLowerCase().indexOf(formats[1])).trim();
            } catch (Exception ex) {
                D.e("==>" + task.toString());
                ex.printStackTrace();
            }
            infoModel.bookFormat = formats[0];
            infoModel.bookType = task.cate;
            infoModel.pageUrl = url;

            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            taskModel.delayTime = 1500;
            taskModel.infoModel = infoModel;
            taskModel.testSite = true;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseInfo(TaskModel task) {
        Element downEle = task.resDoc.selectFirst("#erphpdown > div > a");
        String url = downEle.attr("href");
        TaskModel taskModel = createTask(DOWN);
        taskModel.url = url;
        taskModel.delayTime = 1500;
        taskModel.infoModel = task.infoModel;
        addHttpTask(taskModel);
    }

    @Override
    protected void parseDown(TaskModel task) {
        Elements downEles = task.resDoc.select("#focus > div > div.ordown-header > div.ordown-buy > a");
        for (Element e : downEles) {
            if (e.text().contains("下载")) {
                String url = e.attr("href");
                task.infoModel.addDownModel(new DownModel(url, (url.contains(CTFILE1) || url.contains(CTFILE2))
                        ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));
                break;
            }
        }

        if (task.infoModel.downModel.isEmpty()) {
            D.e("获取下载地址失败==>" + task.url);
            return;
        }

        if (D.DEBUG)
            D.i("==>" + task.infoModel);

        saveBook(task.infoModel);
    }
}
