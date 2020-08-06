package com.books.pdf80;

import com.books.utils.BookApp;
import com.books.utils.DownModel;
import com.company.core.App;
import com.books.utils.InfoModel;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.HttpUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

//需要验证码
public class Pdf80App extends BookApp {
    private static final String CODE = "8080";

    private Map<String, String> headers = new HashMap<>();

    private static final String DOWN_PAGE = "downPage";

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
        }
    }

    private void parseDown(TaskModel task) {
        Elements downEles = task.resDoc.select("body > div.wrap > div.con > div.lc_hidebox.mustvip.viewon > div.boxbody > p > a");
        InfoModel model = new InfoModel();
        model.pageUrl = task.url;
        //TODO 没bookName
        for (Element down : downEles) {
            if (down.text().contains("PDF")) {
                String url = down.attr("href");
                model.downModel.add(new DownModel(url));
                break;
            }
        }

        saveBook(model);
        D.p("80pdf==>" + model.toString());
    }

    private void parseInfo(TaskModel task) {
        //<a rel="nofollow" href="/index.php?c=download&amp;id=6502&amp;timestamp=1596188915" target="_blank"><i class="fa fa-angle-down"></i>《三毛全集20-高原的百合花》下载</a>
        //https://590m.com/file/25158122-433080233
        Element downEle = task.resDoc.selectFirst("#contentleft > div > div.downarea > a:nth-child(2)");
        String downlaodPage = downEle.attr("href");
        ///index.php?c=download&id=6502&timestamp=1596197397
        String start = "id=";
        String dataId = downlaodPage.substring(downlaodPage.indexOf(start) + start.length(), downlaodPage.indexOf("&timestamp"));

        Map<String, String> params = new HashMap<>();
        params.put("code", CODE);
        params.put("id", dataId);

        TaskModel taskModel = createTask(DOWN_PAGE);
        taskModel.url = "http://www.80pdf.com" + downlaodPage;
        taskModel.requestType = HttpUtils.POST;
        taskModel.headers = headers;
        taskModel.headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        addHttpTask(taskModel);
    }

    private void parseHome(TaskModel task) {
        Elements items = task.resDoc.select("#contentleft > div > div.logcon > div.hk-archives > ul > li > ul > li > a");
//        D.p("==>" + items.size() + "  url==>" + items.get(0).attr("href"));
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
}
