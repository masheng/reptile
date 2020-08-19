package com.work.books.apps;

import com.work.books.utils.*;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页即是所有的书目http://pdf.018zy.com
 * <p>
 * 分类中只是进行了分类 没有新书
 */
public class ZYApp extends BookApp {
    private static final String CTFILE = "https://306t.com/";

    private Map<String, String> headers = new HashMap<>();

    public static void main(String[] args) {
        ZYApp zy = new ZYApp();

        zy.startSingle();
    }

    @Override
    public void config() {
        super.config();
        headers.put("Host", "pdf.018zy.com");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headers.put("Accept-Language", "zh-cn");
        headers.put("Accept-Encoding", "gzip, deflate");

        TaskModel task = createTask(HOME);
        task.url = "http://pdf.018zy.com";
        task.headers = headers;
        addHttpTask(task);
    }

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case HOME:
                parseHome(task);
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
        parseList(task);

        Document doc = task.resDoc;
        Element pageEle = doc.selectFirst("#Central > div.wp-pagenavi > a.last");
        String pageUrl = pageEle.attr("href");
        String countPage = pageUrl.substring(pageUrl.lastIndexOf("/") + 1);
        int count = Integer.parseInt(countPage);

        scanInfoModel.cateInfo.put(DEFAULT_SCAN_CATE, new ScanInfoModel.ScanInfo(DEFAULT_SCAN_CATE, count));
        count = pageCountOff(count, DEFAULT_SCAN_CATE, task.url);

        String baseUrl = "http://pdf.018zy.com/page/";
        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = baseUrl + i;
            taskModel.headers = headers;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseList(TaskModel task) {
        Document doc = task.resDoc;
        Elements lists = doc.select("div.postloop_body_post > h3.post_title > a");
        for (Element item : lists) {
            TaskModel taskModel = createTask(INFO);
            taskModel.url = item.attr("href");
            taskModel.headers = headers;
            taskModel.delayTime = 1000;

            if (BookDBUtls.testSaveSiteInfo(taskModel.url))
                addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    private void parseInfo(TaskModel task) {
        Document doc = task.resDoc;
        Element url = doc.selectFirst("p.downlink > strong > a.downbtn");
        InfoModel model = new InfoModel();
        model.pageUrl = task.url;

        if (url != null) {
            String downUrl = url.attr("href");
            String name = url.attr("title");
            model.bookName = name;
            if (name.contains("《")) {
                model.bookName = name.substring(name.indexOf("《") + 1, name.indexOf("》"));
            }
            if (name.contains("[")) {
                model.bookFormat = name.substring(name.lastIndexOf("[") + 1, name.lastIndexOf("]"));
            }
            model.addDownModel(new DownModel(downUrl, downUrl.startsWith(CTFILE) ? BookConstant.CTFILE_PAN : BookConstant.PRIVATE_PAN));

            D.i("info==>" + model.toString());
            saveBook(model);
        }
    }
}
