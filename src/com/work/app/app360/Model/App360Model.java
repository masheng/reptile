package com.work.app.app360.Model;

import com.company.core.TaskModel;

/**
 * Created by ms on 2017/7/31.
 */
public class App360Model extends TaskModel {
    public int total;
    public String cat_name;
    //app类型 如影音、社交
    public int csid;
    public String cid;
    public int index;

    @Override
    public String toString() {
        return "App360Model{" +
                "total=" + total +
                ", cat_name='" + cat_name + '\'' +
                ", csid='" + csid + '\'' +
                ", cid='" + cid + '\'' +
                ", index=" + index +
                '}';
    }
}
