package com.work.app.tengxun;

import com.company.core.App;
import com.company.core.STEP;
import com.company.core.TaskModel;

/**
 * Created by ms on 2017/9/10.
 */
public class AppTengXun extends App{
    @Override
    public void parse(TaskModel taskModel) {

    }

    @Override
    public void failed(TaskModel taskModel) {

    }

    @Override
    protected void firstPage() {
        TaskModel task = new TaskModel();
        task.app = this;
        task.step = STEP.first;
        task.url = "http://android.myapp.com/myapp/cate/appList.htm?orgame=1&categoryId=0&pageSize=20&pageContext=0";
    }
}
