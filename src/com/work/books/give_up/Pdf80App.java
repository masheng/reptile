package com.work.books.give_up;

import com.work.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.HttpUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

//需要验证码
public class Pdf80App extends BookApp {
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private static final String CTFILE = "474b.com";

    private static final String DOWN_PAGE = "downPage";
    private static final String DOWN_PAGE1 = "downPage1";

    public static void main(String[] args) {
        Pdf80App app = new Pdf80App();
        app.startSingle();

    }

    @Override
    protected void config() {
        super.config();

        headers.put("Host", "www.80pdf.com");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headers.put("Accept-Language", "zh-cn");
        headers.put("Accept-Encoding", "gzip, deflate");

        params.put("code", ConfigUtils.PDF80_CODE);

        TaskModel task = createTask(HOME);
        task.url = "http://www.80pdf.com/allBooks.html";
        task.headers = headers;
        addHttpTask(task);
    }

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case HOME:
                parseHome(task);
                break;
            case INFO:
                parseInfo(task);
                break;
            case DOWN_PAGE:
                parseDown(task);
                break;
            case DOWN_PAGE1:
                parseDown1(task);
                break;
        }
    }

    private void parseHome(TaskModel task) {
        Elements items = task.resDoc.select("#contentleft > div > div.logcon > div.hk-archives > ul > li > ul > li > a");
//        D.i("==>" + items.size() + "  url==>" + items.get(0).attr("href"));
        //http://www.80pdf.com/index.php?c=content&a=show&id=6497
        //                    /index.php?c=content&a=show&id=6502
        for (Element item : items) {
            TaskModel taskModel = createTask(INFO);
            String url = item.attr("href");
            taskModel.url = "http://www.80pdf.com" + url;
            taskModel.headers = headers;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        //<a rel="nofollow" href="/index.php?c=download&amp;id=6502&amp;timestamp=1596188915" target="_blank"><i class="fa fa-angle-down"></i>《三毛全集20-高原的百合花》下载</a>
        //https://590m.com/file/25158122-433080233
        Element downEle = task.resDoc.selectFirst("#contentleft > div > div.downarea > a:nth-child(2)");
        String downlaodPage = downEle.attr("href");
        ///index.php?c=download&id=6502&timestamp=1596197397
        String start = "id=";
        String dataId = downlaodPage.substring(downlaodPage.indexOf(start) + start.length(), downlaodPage.indexOf("&timestamp"));
        params.put("id", dataId);

        InfoModel infoModel = new InfoModel();
        infoModel.bookName = task.resDoc.selectFirst("#contentleft > div > h1").text();
        if (infoModel.bookName.contains("《"))
            infoModel.bookName = StrUtils.subStr(infoModel.bookName, "《", "》", true);
        infoModel.bookImg = task.resDoc.selectFirst("#contentleft > div > div.logcon > p > img").attr("src");
        infoModel.bookFormat = BookConstant.F_PDF;

        TaskModel taskModel = createTask(DOWN_PAGE);
        taskModel.url = "http://www.80pdf.com" + downlaodPage;
        taskModel.requestType = HttpUtils.GET;
        taskModel.headers = headers;
        taskModel.infoModel = infoModel;
        taskModel.headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        addHttpTask(taskModel);
    }

    private void parseDown(TaskModel task) {
        task.tag = DOWN_PAGE1;
        task.requestType = HttpUtils.POST;
        task.params = params;
        addHttpTask(task);
    }

    private void parseDown1(TaskModel task) {
        Elements downEles = task.resDoc.select("body > div.wrap > div.con > div.lc_hidebox.mustvip > div.boxbody > p > a");
        task.infoModel.pageUrl = task.url;
        for (Element down : downEles) {
            if (down.text().contains("购买"))
                continue;

            String url = down.attr("href");
            int type = url.contains(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN;
            task.infoModel.addDownModel(new DownModel(url, type));
        }

        D.i("80pdf==>" + task.infoModel.toString());

        saveBook(task.infoModel);
    }

}
