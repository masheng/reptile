package com.company.core;


import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.ThreadUtils;
import com.sun.istack.internal.NotNull;

/**
 * Created by ms on 2017/9/9.
 */
public abstract class App {
    protected ThreadUtils threadUtils = new ThreadUtils();

    public boolean addHttpTask(TaskModel model) {
        return threadUtils.addTask(model);
    }

    //开始任务
    public void start(ThreadUtils.Config config) {
        threadUtils.init(config);
        config();
        threadUtils.waitFinish();
    }

    public void start() {
        start(new ThreadUtils.Config());
    }

    public void startSingle() {
        ThreadUtils.Config config = new ThreadUtils.Config();
        config.DEFAULT_CORE_THREAD_SIZE = 1;
        config.DEFAULT_MAX_THREAD_SIZE = 1;

        threadUtils.init(config);
        config();
        threadUtils.waitFinish();
    }

    protected TaskModel createTask(String tag) {
        TaskModel taskModel = new TaskModel(this, tag);
        taskModel.desc = getClass().getName();
        return taskModel;
    }

    protected abstract void config();

    public boolean check(TaskModel task) {
        return true;
    }

    public abstract void parse(@NotNull TaskModel task);

    public void onFailed(TaskModel task) {
        D.e(String.format("app:%s tag:%s err:%s url:%s", task.desc, task.tag, task.errMsg, task.url));
    }
}
