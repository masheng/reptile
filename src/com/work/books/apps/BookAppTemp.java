package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.work.books.utils.BookApp;

public abstract class BookAppTemp extends BookApp {

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case HOME:
                parseHome(task);
                break;
            case CATEGORY:
                parseCate(task);
                break;
            case LIST:
                parseList(task);
                break;
            case INFO:
                parseInfo(task);
                break;
            case DOWN:
                parseDown(task);
                break;
        }
    }

    protected abstract void parseHome(TaskModel task);

    protected void parseCate(TaskModel task) {
    }

    protected abstract void parseList(TaskModel task);

    protected abstract void parseInfo(TaskModel task);

    protected void parseDown(TaskModel task) {
    }
}
