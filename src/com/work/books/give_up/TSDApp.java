package com.work.books.give_up;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.apps.BookAppTemp;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TSDApp extends BookAppTemp {
    public static void main(String[] args) {
        TSDApp app = new TSDApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel taskModel = createTask(HOME);
        taskModel.url = "https://www.taoshudang.com";
        addHttpTask(taskModel);
    }

    @Override
    protected void parseHome(TaskModel task) {
        Elements cateEles = task.resDoc.select("#menu-caidan > li > a");
        for (Element e : cateEles) {
            TaskModel taskModel = createTask(CATEGORY);
            taskModel.url = e.attr("href");
            taskModel.cate = e.text();
            addHttpTask(taskModel);
        }
    }

    @Override
    protected void parseCate(TaskModel task) {
        parseList(task);

        Element pageCountEle = task.resDoc.selectFirst("#primary > div > nav > div > a:nth-last-child(2)");
        String pageCountStr = pageCountEle.text();
        String pageCount = pageCountStr.split(" ")[1];
        int count = Integer.parseInt(pageCount);

        count = cateCount(task.cate, task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = String.format("%spage/%d/", task.url, i);
            taskModel.cate = task.cate;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {

    }

    @Override
    protected void parseInfo(TaskModel task) {

    }
}
