package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

//http://www.dzs.so
public class DZSApp extends BookAppTemp {
    private Map<String, String> params = new HashMap<>();
    private Map<String, Integer> cateIndex = new HashMap<>();

    public static void main(String[] args) {
        DZSApp app = new DZSApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        params.put("huoduan_verifycode", ConfigUtils.DZS_CODE);

        TaskModel task = createTask(HOME);
        task.url = "http://www.dzs.so";
        addHttpTask(task);
    }

    @Override
    protected void parseHome(TaskModel task) {
        Elements cateEles = task.resDoc.select("body > section > header > ul.nav > li > a");
        for (Element e : cateEles) {
            String cate = e.text();
            if (cate.contains("看世界") || cate.contains("赚钱") || cate.contains("教程"))
                continue;

            synchronized (this) {
                cateIndex.put(cate, 1);
            }

            TaskModel taskModel = createTask(LIST);
            taskModel.url = e.attr("href");
            taskModel.cate = cate;
            taskModel.obj = taskModel.url;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {
        //没有总页数 只有下一页
        Element nextEle = task.resDoc.selectFirst("body > section > div > div > div.pagination > ul > li.next-page > a");
        if (nextEle != null) {
            int index = 0;
            synchronized (this) {
                index = cateIndex.get(task.cate);
                cateIndex.put(task.cate, ++index);
            }
            TaskModel taskModel = createTask(LIST);
            taskModel.url = String.format("%spage/%d/", task.obj, index);
            taskModel.cate = task.cate;
            taskModel.obj = task.obj;
            addHttpTask(taskModel);
        }

        Elements listEles = task.resDoc.select("body > section > div > div > article > header > h2 > a");
        for (Element e : listEles) {
            TaskModel taskModel = createTask(INFO);
            taskModel.url = e.attr("href");
            taskModel.params = params;
            taskModel.testSite = true;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseInfo(TaskModel task) {
        InfoModel infoModel = new InfoModel();
        infoModel.pageUrl = task.url;

        //信息
        Elements infoEles = task.resDoc.select("body > section > div > div > article > p");
        for (Element e : infoEles) {
            String info = e.text();
            if (info.contains("书名：")) {
                infoModel.bookName = info.substring("书名：".length());
            } else if (info.contains("作者：")) {
                infoModel.bookAuthor = info.substring("作者：".length());
            } else if (info.contains("格式：")) {
                infoModel.bookFormat = info.substring("格式：".length());
            }
        }

        if (StrUtils.isEmpty(infoModel.bookName)) {
            Element nameEle = task.resDoc.selectFirst("body > section > div > div > header > h1 > a");
            infoModel.bookName = nameEle.text();
        }

        //下载地址
        Elements downEles = task.resDoc.select("body > section > div > div > article > div");
        for (Element e : downEles) {
            if (e.text().contains("链接") && e.text().contains("提取码")) {
                String down = "", code;
                String downStr = e.text();
                //<div style="border:1px dashed #F60; padding:10px; margin:10px 0; line-height:200%;  background-color:#FFF4FF; overflow:hidden; clear:both;"><!--wechatfans start-->链接：https://pan.baidu.com/s/1j584cDvumVZLz4Lqb1-BuA<br>
                //提取码：h05j<!--wechatfans end--></div>
                String[] downUrl = null;
                if (downStr.contains("提取码："))
                    downUrl = e.text().split("提取码：");
                else if (downStr.contains("提取码:"))
                    downUrl = e.text().split("提取码:");
                else
                    D.e("获取提取码出错==>" + task.url);

                if (downUrl[0].contains("<br>"))
                    downUrl[0] = downUrl[0].substring(0, downUrl[0].lastIndexOf("<br>"));
                if (downUrl[0].contains("链接："))
                    down = downUrl[0].substring("链接：".length());
                else if (downUrl[0].contains("链接:"))
                    down = downUrl[0].substring("链接:".length());
                else
                    D.e("获取下载地址出错==>" + task.url);

                downUrl[1] = downUrl[1].trim();
                code = downUrl[1].substring(0, 4);

                infoModel.addDownModel(new DownModel(down, code, down.contains("pan.baidu") ? BookConstant.BAIDU_PAN : BookConstant.PRIVATE_PAN));


            }
        }

        if (D.DEBUG)
            D.i("==>" + infoModel.toString());

        saveBook(infoModel);
    }
}
