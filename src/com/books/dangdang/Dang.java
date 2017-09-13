package com.books.dangdang;

import com.company.core.App;
import com.company.core.STEP;
import com.company.core.TaskModel;

/**
 * Created by ms on 2017/9/12.
 */
//http://category.dangdang.com/?ref=www-0-C
public class Dang extends App{
    @Override
    public void parse(TaskModel taskModel) {
        switch (taskModel.step) {
            case first:

                break;
        }
    }

    @Override
    public void failed(TaskModel taskModel) {

    }

    @Override
    protected void firstPage() {
        TaskModel taskModel = new TaskModel("http://category.dangdang.com/?ref=www-0-C", this, STEP.first);

    }
}
