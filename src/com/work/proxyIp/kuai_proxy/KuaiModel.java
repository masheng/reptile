package com.work.proxyIp.kuai_proxy;

import com.company.core.App;
import com.company.core.STEP;
import com.company.core.TaskModel;

import java.util.Date;

/**
 * Created by ms on 2017/9/10.
 */
public class KuaiModel extends TaskModel{
    public KuaiModel(){}
    public KuaiModel(String url, App app, STEP step) {
        super(url, app, step);
    }

    public String ip;
    public int port;
    public int grade;
    public String level;
    public String lastUpdate;
    public int speed;
    public int test;
    public String location;
    public String type;
    public Date insertTime;

    public void reset(){
        ip = null;
        port = 0;
        grade = 0;
        level = null;
        lastUpdate = null;
        speed = 0;
        test = 0;
        location = null;
        type = null;
        insertTime = null;
    }

    @Override
    public String toString() {
        return "KuaiModel{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", grade=" + grade +
                ", level=" + level +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", speed=" + speed +
                ", test=" + test +
                ", location='" + location + '\'' +
                ", type=" + type +
                '}';
    }
}
