package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.HttpUtils;
import com.work.books.utils.BookApp;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
//sudo keytool -import -file /Users/ms/share/project/pro/reptile/reptile/src/com/work/books/apps/cer/jr.cer -keystore /Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/jre/lib/security/cacerts -alias jr_server -storepass changeit
//keytool -list -keystore "/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/jre/lib/security/cacerts"| findstr /i jr_server
//keytool -list -keystore "/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/jre/lib/security/cacerts"| grep /i jr_server
//keytool -delete -alias jr_server -keystore "/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/jre/lib/security/cacerts" -storepass changeit
public class JRApp extends BookApp {
    private static String HomePage = "https://oss.achirou.workers.dev/0:/";
//    private static String HomePage = "https://books.andrewjr.wang/0:/";
    Map<String, String> headers = new HashMap<>();
    Map<String, String> params = new HashMap<>();

    public static void main(String[] args) {
        JRApp app = new JRApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        headers.put("accept", "application/json, text/plain, */*");
        headers.put("content-type", "application/json;charset=utf-8");
//        headers.put("origin", "https://books.andrewjr.wang");
        headers.put("accept-language", "zh-cn");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headers.put("accept-encoding", "gzip, deflate");
//        headers.put("referer", "https://books.andrewjr.wang/0:/");

        params.put("q", "");
        params.put("password", null);
        params.put("page_token", null);
        params.put("page_index", "0");

        TaskModel taskModel = createTask(HOME);
        taskModel.url = HomePage;
        taskModel.requestType = HttpUtils.GET;
        taskModel.headers = headers;
//        taskModel.params = params;
        addHttpTask(taskModel);
    }

    @Override
    public void parse(TaskModel task) {
        D.i("res==>" + task.response);
//        Elements cates = task.resDoc.select("#app > div > section > div > div > div.golist > table > tbody > tr > td > svg > use");
//        for (Element c : cates) {
//            D.i("==>" + c.attr("href"));
//        }
    }
}
