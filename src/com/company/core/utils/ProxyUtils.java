package com.company.core.utils;

import com.work.proxyIp.kuai_proxy.KuaiModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ms on 2017/9/11.
 */
public class ProxyUtils {
    private final static int LEN = 100;
    private static ArrayList<ProxyTestModel> queue = new ArrayList(LEN+10);
    private static int index = 0;
    public static ProxyTestModel getProxy() {
        ProxyTestModel kuaiModel = queue.get((index++)%queue.size());
//        if(kuaiModel.useCount > 8)
//            if(kuaiModel.success < kuaiModel.useCount/2+2) {
//                queue.remove(kuaiModel);
//                D.p("remove ==>"+kuaiModel.toString());
//                return getProxy();
//            }

        return kuaiModel;
    }

    public static void clear(){
        for (ProxyTestModel ptm:queue)
            ptm = null;

        queue = null;
    }

    public static int debug(){
        for (ProxyTestModel ptm:queue)
            D.p(String.format("-use:%d-suc:%d-sp:%d--ip:%s", ptm.useCount, ptm.success, ptm.ms, ptm.kuaiModel.ip));

        D.p("len==>"+queue.size());
        return 0;
    }

    public static void init(){
        ProxyTestModel ptm1 = new ProxyTestModel();
        ptm1.kuaiModel.ip = "121.31.101.73";
        ptm1.kuaiModel.port = 8123;
        queue.add(ptm1);
        ptm1 = new ProxyTestModel();
        ptm1.kuaiModel.ip = "222.208.83.175";
        ptm1.kuaiModel.port = 9000;
        queue.add(ptm1);
        ptm1 = new ProxyTestModel();
        ptm1.kuaiModel.ip = "121.232.146.70";
        ptm1.kuaiModel.port = 9000;
        queue.add(ptm1);


        PreparedStatement state = null;
        try {
            String sql = "select * from kuai_proxy WHERE speed < 3 order by lastUpdate ASC limit "+LEN+";";
            state = DatabaseUtils.getConn(DatabaseUtils.PROXY).prepareStatement(sql);
            ResultSet rs = state.executeQuery(sql);
            while (rs.next()) {
                ProxyTestModel ptm = new ProxyTestModel();

                ptm.kuaiModel.ip = rs.getString("ip");
                ptm.kuaiModel.port = rs.getInt("port");
                ptm.kuaiModel.speed = rs.getInt("speed");
                queue.add(ptm);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(state!=null)
                try {
                    state.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }


}
