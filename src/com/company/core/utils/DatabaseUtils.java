package com.company.core.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ms on 2017/7/31.
 */
public class DatabaseUtils {
    private static final Map<String, Connection> conns = new ConcurrentHashMap<>();

    //根据数据库名称获取Connection对象
    public static synchronized Connection getConn(String database) {
        Connection connection = conns.get(database);
        if (connection == null) {
            synchronized (conns) {
                if (connection == null) {
                    connection = getRealConn(database);
                    if (connection == null)
                        throw new NullPointerException("无法连接数据库");

                    conns.put(database, connection);
                }
            }
        }
        return connection;
    }

    public static synchronized void closeConn(String db) {
        Connection conn = conns.get(db);
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    public static synchronized void closeAllConn() {
        for (Connection conn : conns.values())
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
    }

    private static Connection getRealConn(String database) {
        Connection conn = null;
        //驱动程序名
        String driver = "com.mysql.cj.jdbc.Driver";
//        String driver = "com.mysql.jdbc.Driver";
        //URL指向要访问的数据库名mydata
        String url = "jdbc:mysql://localhost:3306/" + database + "?useUnicode=true&serverTimezone=UTC&useSSL=false";
        //MySQL配置时的用户名
        String user = "root";
        //MySQL配置时的密码
        String password = "123456";
        //遍历查询结果集
        //加载驱动程序
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
