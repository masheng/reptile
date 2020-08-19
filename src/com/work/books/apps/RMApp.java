package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.work.books.utils.BookApp;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//http://moreread.me
public class RMApp extends BookApp {
    public static void main(String[] args) {
        RMApp app = new RMApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        TaskModel taskModel = createTask(HOME);
        taskModel.url = "http://moreread.me";
        addHttpTask(taskModel);
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
                break;
        }
    }

    private void parseHome(TaskModel task) {
        parseList(task);

        Element pageCountEle = task.resDoc.selectFirst("#dataTable_paginate > ul > li:nth-last-child(2) > a");
        String pageCount = pageCountEle.text();
        int count = Integer.parseInt(pageCount);

        count = cateCountDefault(task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = "http://moreread.me/?page=" + i;
            addHttpTask(taskModel);
        }
    }

    private void parseList(TaskModel task) {
        Elements listEles = task.resDoc.select("body > div.site-wrap > div.site-section > div > div:nth-child(2) > div > div > a");
        for (Element e : listEles) {
            TaskModel taskModel = createTask(INFO);
            taskModel.url = e.attr("href");
            addHttpTask(taskModel);
        }
    }

    private void parseInfo(TaskModel task) {
        Elements downEles = task.resDoc.select("body > div > div.site-section > div.container > div > div.main-content > div.pt-5 > ul > li > div.comment-body");

    }
}
