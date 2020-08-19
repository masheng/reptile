package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.HttpUtils;
import com.work.books.utils.*;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class S52App extends BookAppTemp {
    private AtomicInteger index = new AtomicInteger(1);
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> headersDown1 = new HashMap<>();
    private Map<String, String> paramsDown1 = new HashMap<>();

    private static final String DOWN1 = "down1";
    private static final String DOWN2 = "down2";

    public static void main(String[] args) {
        S52App app = new S52App();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();
        //
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headers.put("accept-encoding", "gzip, deflate");
        headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//
        headersDown1.put("Host", "52sharing.lanzous.com");
        headersDown1.put("Accept", "application/json, text/javascript, */*");
        headersDown1.put("X-Requested-With", "XMLHttpRequest");
        headersDown1.put("Accept-Encoding", "gzip, deflate");
        headersDown1.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");
        headersDown1.put("Content-Type", "application/x-www-form-urlencoded");

        params.put("huoduan_verifycode", ConfigUtils.S52_CODE);
        nextPage();
    }

    @Override
    protected void parseHome(TaskModel task) {

    }

    @Override
    protected void parseList(TaskModel task) {
        Element lastPageEle = task.resDoc.selectFirst("body > section > div.content-wrap > div > div > ul > li.next-page > a");
        if (lastPageEle != null)
            nextPage();

        Elements listEles = task.resDoc.select("body > section > div.content-wrap > div > article > header > h2 > a");
        for (Element e : listEles) {
            TaskModel taskModel = createTask(INFO);
            taskModel.testSite = true;
            taskModel.url = e.attr("href");
            taskModel.requestType = HttpUtils.POST;
            taskModel.headers = headers;
            taskModel.params = params;

            String title = e.text();
            InfoModel infoModel = new InfoModel();
            infoModel.pageUrl = taskModel.url;
            parseName(infoModel, title);

            taskModel.infoModel = infoModel;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseInfo(TaskModel task) {
        Element downEle = task.resDoc.selectFirst("body > section > div.content-wrap > div > article a.dl");
        String url = "", code = "";
        if (downEle != null) {
            url = downEle.attr("href");
            String codeStr = downEle.text();
            if (codeStr.contains("密码") || codeStr.contains("提取码")) {
                codeStr = codeStr.split("：")[1].trim();
                code = codeStr.substring(0, 4);
            }
        } else {
            downEle = task.resDoc.selectFirst("body > section > div.content-wrap > div > article > table > tbody > tr:nth-child(3) > td > a");
            if (downEle != null) {
                url = downEle.attr("href");
            }
        }
        //地址实时变动 解析出来也无法存储 解析好像也有问题 恶心
//        if (url.contains("52sharing")) {
//            //动态
//            TaskModel taskModel = createTask(DOWN);
//            taskModel.url = url;
//            taskModel.infoModel = task.infoModel;
//            taskModel.headers = headers;
//            addHttpTask(taskModel);
//        } else {
        int panType = BookConstant.isBaiduPan(url) ? BookConstant.BAIDU_PAN : BookConstant.PRIVATE_PAN;
        if (url.contains("52sharing"))
            panType = BookConstant.CHANG_PAN;
        task.infoModel.addDownModel(new DownModel(url, code, panType));

        D.i("==>" + task.infoModel.toString());

        saveBook(task.infoModel);
//        }
    }

    @Override
    protected void parseDown(TaskModel task) {
        if (StrUtils.isEmpty(task.response)) {
            D.ee("parseDown返回空  url==>" + task.url);
            return;
        }

        Element downEle = task.resDoc.selectFirst("iframe.ifr2");
        String sign = downEle.attr("src");

        TaskModel taskModel = createTask(DOWN1);
        taskModel.headers = headers;
        //Host	52sharing.lanzous.com
        taskModel.url = "https://52sharing.lanzous.com" + sign;
        taskModel.infoModel = task.infoModel;
        addHttpTask(taskModel);

//
//        int panType = task.url.contains("lanzous") ? BookConstant.LZ_PAN : BookConstant.PRIVATE_PAN;
//        task.infoModel.addDownModel(new DownModel(url, panType));
//
//        D.i("==>" + task.infoModel.toString());
//
//        saveBook(task.infoModel);
    }

    private void parseDown1(TaskModel task) {
//        D.ee("==>" + task.response);
        int start = task.response.indexOf("sign\':\'") + "sign\':\'".length();
        int end = task.response.indexOf("\',\'ves");
        String sign = task.response.substring(start, end);
//        D.ee("sign==>" + sign);
        TaskModel taskModel = createTask(DOWN2);
        taskModel.url = "https://52sharing.lanzous.com/ajaxm.php";
        paramsDown1.put("action", "downprocess");
        paramsDown1.put("sign", sign);
        paramsDown1.put("ves", "1");
        taskModel.params = paramsDown1;
        refreshModified();
        taskModel.headers = headers;
        taskModel.requestType = HttpUtils.POST;
        taskModel.parse = false;
        taskModel.infoModel = task.infoModel;
        addHttpTask(taskModel);
    }

    private void parseDown2(TaskModel task) {
        JSONObject obj = new JSONObject(task.response);
        String url = obj.getString("dom") + "/com/work/books/apps/file/" + obj.getString("url");
        D.ee(task.infoModel.pageUrl + "   d url==>" + url);
    }

    @Override
    public void parse(TaskModel task) {
        super.parse(task);
        switch (task.tag) {
            case DOWN1:
                parseDown1(task);
                break;
            case DOWN2:
                parseDown2(task);
                break;
        }
    }

    private void nextPage() {
        if (index.get() >= 10 && D.DEBUG)
            return;

        TaskModel taskModel = createTask(LIST);
        int pageIndex = index.getAndIncrement();
        if (pageIndex == 1)
            taskModel.url = "https://52sharing.cn/category/book";
        else
            taskModel.url = "https://52sharing.cn/category/book/page/" + pageIndex;
        addHttpTask(taskModel);
    }


    private void parseName(InfoModel infoModel, String title) {
        if (title.contains("《")) {
            infoModel.bookName = StrUtils.subStr(title, "《", "》", true);
        }

        if (title.contains("（作者）")) {
            infoModel.bookAuthor = StrUtils.subStr(title, "》", "（作者）", true);
        }

        //解析格式
        String format[] = testFormat(title);
        if (!StrUtils.isEmpty(format[1])) {
            infoModel.bookFormat = format[0];

            if (StrUtils.isEmpty(infoModel.bookName))
                infoModel.bookName = title.substring(0, title.toLowerCase().indexOf(format[1]));
        }
        if (StrUtils.isEmpty(infoModel.bookName))
            infoModel.bookName = title;
    }

    //If-Modified-Since	Tue, 11 Aug 2020 11:30:00 GMT
    private void refreshModified() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE，dd MMM yyyy HH：mm：ss Z");
        String time = dateFormat.format(new Date());
        headers.put("If-Modified-Since", time);
    }
}


//<a href="https://vip.d0.baidupan.com/file/?VzFXaVtqVWQEDVBoCz4GagQ7AjoCuFH9Aa1Qv1eIVfxStQHoANMOswfKA/8KuVXvUYQE5APZC7FVtwCiU6gB5VfSV6hb71WaBNRQ6QvjBo8EuwKiAspRuwGlUNNX7lXRUuMBZwBlDr8H1wPZCrFV6FGCBCEDcQspVbcAqFOkAelX1FeoW+FVvwTcUO8LxAatBHACZgInUSQBNVBwV21VPVI+ATMAAg4zBzUDaApsVWNRPQQ4A2sLPlVhADRTKQFmVyRXPVs+VTYEZ1A1C20GMgRsAiECLVEjATtQZFc7VWZSYgF5AG0OYgd+A2UKZ1V7UTwEOANsCztVMQBkUzYBYlcyVzVbM1VjBDdQZwttBjMEPwJjAm5RNwFkUGBXOVU1UmEBYwBuDjsHYgMxCmZVbFElBGADJgt+VWEAIFN6AXNXMldyW29VZQRvUD4LbwY/BG4CMAJpUXUBclA/V2ZVMVI0AWsAbA5vB2gDYgpvVWFROQQzA2gLP1V8AChTKQFmVztXd1s7VTAEZ1A/C2gGNgRqAjQCZFFhAT9QcFd+VSRSJQFrAGwObwdoA2MKZ1VsUT4ENwNqCzhVdABzU2YBcFdqVzFbNFU1BHxQNAtvBjAEcAI1AmtRfQEwUG8=" target="_blank" rel="noreferrer"><span class="txt">电信下载</span><span class="txt txtc">联通下载</span><span class="txt">普通下载</span></a>
//https://5https://vip.d0.baidupan.com/file/?B2FRb1loVWQJAFFpVmNVOQc4V28FvwOkUfNUtAbMU/1VsVG+CNQCv1SZBtYHvFzhB9VTslaxA5cF5VPAU4NRtQeXUYtZ7FWQCchR4laSVdkHtVf9BcsD51HAVNUGv1PoVcZRJgiwAtNUqga1B8hcxge4U9BWhQPuBb5T3FMvUX0HJ1HhWapVgQmdUbBW51XxB/hXtAXMA65RslTqBttTfFU0UXwIIgJlVCQGNgc6XDEHOFMJVmADZQU/U2dTOFFiBzZRNFk9VTEJaFEhVmRVJwdgV2MFbwM5UWFUYAZuU2pVc1F2CCUCa1QwBmAHYVxtB3JTZlY+Ay4FMVNkUyBRNAc3UTBZMVU5CTtRNlYyVWIHP1czBW8DYlE1VGMGOFNkVWxRNAhhAjBUZgZlB2FcaAdrU2NWPwNgBTBTM1M+UX4HZlF8WXNVMgl5UXJWcVUxBy9XOwU+AzxRblRiBmJTYlViUTIIcwIiVGsGPQc2XDsHYFNnVjkDMQUyU2VTIVF+ByFRYFloVXYJYlE2VjdVbQdqV2MFbwMxUWVUZgZrU3RVIFF2CCICa1QzBmUHalxrB2xTb1YxAzYFMVNmUylRJQduUXZZOVUwCW1RM1YsVWYHbVdlBXQDM1FgVHwGbFNr