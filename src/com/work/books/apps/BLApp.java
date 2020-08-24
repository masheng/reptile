package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.DownModel;
import com.work.books.utils.InfoModel;
import com.work.books.utils.StrUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class BLApp extends BookAppTemp {
    private Map<String, String> headers = new HashMap<>();

    public static void main(String[] args) {
        BLApp app = new BLApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headers.put("accept-encoding", "gzip, deflate");

        TaskModel task = createTask(HOME);
        task.url = "https://bloogle.top/mulu/";
        task.headers = headers;
        addHttpTask(task);
    }

    @Override
    protected void parseHome(TaskModel task) {
        parseList(task);

        Element pageCountEle = task.resDoc.selectFirst("#main > div > div.entry-content > div.wp-pagenavi > a.last");
        String pageCountUrl = pageCountEle.attr("href");
        String pageCount = StrUtils.subStr(pageCountUrl, "page/", "/", false);
        int count = Integer.parseInt(pageCount);

        count = cateCountDefault(task.url, count);

        for (int i = 2; i <= count; i++) {
            String url = String.format("https://bloogle.top/mulu/page/%d/", i);
            TaskModel taskModel = createTask(LIST);
            taskModel.url = url;
            taskModel.headers = headers;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {
        Elements listEles = task.resDoc.select("#main > div:nth-child(3) > div > ul > li > span.post-title > a");
        for (Element e : listEles) {
            InfoModel infoModel = new InfoModel();
            String url = e.attr("href");
            //<a href="https://bloogle.top/2019/12/%e3%80%8a%e6%8b%94%e8%92%b2%e6%ad%8c%e3%80%8b%e4%bd%9c%e8%80%85-%e6%b2%88%e4%b9%a6%e6%9e%9d/" title="《拔蒲歌》作者: 沈书枝" target="_blank">《拔蒲歌》作者: 沈书枝</a>
            String title = e.text();
            infoModel.pageUrl = url;
            infoModel.bookName = StrUtils.subStr(title, "《", "》", true);
            if (title.contains("作者:"))
                infoModel.bookAuthor = title.split("作者:")[1];
            if (!StrUtils.isEmpty(infoModel.bookAuthor))
                infoModel.bookAuthor = infoModel.bookAuthor.trim();

            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            taskModel.testSite = true;
            taskModel.infoModel = infoModel;
            taskModel.headers = headers;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseInfo(TaskModel task) {
        Elements downEles = task.resDoc.select("#main > article > div.entry-content > div.wp-block-button > a");
        for (Element e : downEles) {
            String url = e.attr("href");
            String format = e.text();
            if (format.contains("格式")) {
                format = format.substring(0, format.indexOf("格式"));
            }

            InfoModel infoModel = task.infoModel.clone();
            infoModel.bookFormat = format;
            infoModel.addDownModel(new DownModel(url));

            if (D.DEBUG)
                D.i("BL==>" + infoModel.toString());

            saveBook(infoModel);
        }
    }
}
