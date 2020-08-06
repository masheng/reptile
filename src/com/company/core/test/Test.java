package com.company.core.test;

import com.company.core.App;
import com.company.core.model.TaskModel;
import com.company.core.utils.D;

public class Test extends App {
    private static final String STEP1 = "step1";

    @Override
    public void config() {
        TaskModel task = new TaskModel(this, STEP1);
        task.url = "";
        addHttpTask(task);

        //可以请求多个
    }

    @Override
    public boolean check(TaskModel task) {
        switch (task.tag) {
            case STEP1:
                return true;
        }

        return false;
    }

    @Override
    public void parse(TaskModel task) {
        switch (task.tag) {
            case STEP1:
                D.i("res==>" + task.response);
                //解析完成 保存
                save(task.response);
                break;
        }
    }

    public void save(Object model) {

    }

    @Override
    public void onFailed(TaskModel task) {
        switch (task.errCode) {

        }
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.start();
    }
}
