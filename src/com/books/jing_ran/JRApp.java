package com.books.jing_ran;

import com.books.utils.BookApp;
import com.company.core.App;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.HttpUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class JRApp extends BookApp {
    private static String HomePage = "https://books.andrewjr.wang/0:/";
    Map<String, String> headers = new HashMap<>();
    Map<String, String> params = new HashMap<>();

    public static void main(String[] args) {
        JRApp app = new JRApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put("Origin", "");
        headers.put("Accept-Language", "zh-cn");
        headers.put("Host", "books.andrewjr.wang");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Connection", "keep-alive");

        params.put("q", "");
        params.put("password", null);
        params.put("page_token", null);
        params.put("page_index", "0");

        TaskModel taskModel = createTask(HOME);
        taskModel.url = HomePage;
        taskModel.requestType = HttpUtils.POST;
//        taskModel.headers = headers;
//        taskModel.params = params;
        addHttpTask(taskModel);
    }

    @Override
    public void parse(TaskModel task) {
        D.p("res==>" + task.response);
        Elements cates = task.resDoc.select("#app > div > section > div > div > div.golist > table > tbody > tr > td > svg > use");
        for (Element c : cates) {
            D.p("==>" + c.attr("href"));
        }
    }
}
