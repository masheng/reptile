package com.work.books.utils;

import com.company.core.utils.D;
import com.company.core.utils.DatabaseUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BookDBUtls {
    private static Gson gson = new Gson();

    public static boolean insertOrUpBook(InfoModel model) {
        int id = checkBook(model.bookName, model.bookFormat);
        if (id > 0)
            return updateBookDownUrl(id, model);
        else
            return insertBook(model);
    }

    /**
     * 插入书数据
     */
    public static boolean insertBook(InfoModel model) {
        String sql = "insert into book(bookName, bookFormat, downloadUrl" +
                ", pageUrl, insertTime) " +
                "values(?,?,?," +
                "?,?)";
        PreparedStatement state = null;

        if (StrUtils.isEmpty(model.bookName)) {
            D.e("bookName空 url==>" + model.pageUrl);
        }


        String downUlr = "";
        if (model.downModel != null && !model.downModel.isEmpty())
            downUlr = gson.toJson(model.downModel);

        try {
            String bookFormat = model.bookFormat;
            if (StrUtils.isEmpty(bookFormat))
                bookFormat = BookConstant.F_UNKNOW;

            state = DatabaseUtils.getConn(BookConstant.DATA_BASE).prepareStatement(sql);
            state.setString(1, model.bookName);
            state.setString(2, bookFormat.toLowerCase());
            state.setString(3, downUlr);
            state.setString(4, model.pageUrl);
            state.setTimestamp(5, getTimestamp());

            int ret = state.executeUpdate();
            if (ret > 0)
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (state != null) {
                try {
                    state.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 更新下载地址 为info字段 id为checkBook获取
     */
    public static boolean updateBookDownUrl(int id, InfoModel model) {
        if (id <= 0)
            return false;
        if (model.downModel.isEmpty())
            return true;

        Statement state = null;
        ResultSet res = null;
        try {
            state = DatabaseUtils.getConn(BookConstant.DATA_BASE).createStatement();
            String sqlGetInfo = "SELECT downloadUrl FROM book WHERE id = " + id;
            res = state.executeQuery(sqlGetInfo);
            if (res.next()) {
                String downUrls = res.getString("downloadUrl");
                if (downUrls == null)
                    downUrls = "";

                Set<DownModel> urlsSet = new HashSet<>();
                urlsSet.addAll(model.downModel);

                if (!StrUtils.isEmpty(downUrls)) {
                    //去除重复的url
                    Iterator<DownModel> downIter = model.downModel.iterator();
                    while (downIter.hasNext()) {
                        DownModel downModel = downIter.next();
                        if (StrUtils.isEmpty(downModel.urlMD5) || downUrls.contains(downModel.urlMD5))
                            downIter.remove();
                    }

                    if (model.downModel.isEmpty())
                        return true;
                    //如果有新的地址 则加入
                    Type founderSetType = new TypeToken<HashSet<DownModel>>() {
                    }.getType();
                    HashSet<DownModel> founderSet = gson.fromJson(downUrls, founderSetType);
                    urlsSet.addAll(founderSet);
                }
                if (model.downModel.isEmpty())
                    return true;

                String downRes = gson.toJson(urlsSet);

                String sqlUpdateInfo = String.format("UPDATE book SET downloadUrl = '%s' WHERE id = %d", downRes, id);
                int up = state.executeUpdate(sqlUpdateInfo);
                if (up > 0)
                    return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                if (res != null)
                    res.close();
                if (state != null)
                    state.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 判断数据库中是否已存在书名 书类型相同 则返回id
     */
    public static int checkBook(String bookName, String bookFormat) {
        if (StrUtils.isEmpty(bookFormat))
            bookFormat = BookConstant.F_UNKNOW;
        if (StrUtils.isEmpty(bookName))
            return 0;

        Statement state = null;
        ResultSet res = null;
        try {
            state = DatabaseUtils.getConn(BookConstant.DATA_BASE).createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = String.format("SELECT id FROM book WHERE bookName = '%s' AND bookFormat = '%s'", bookName, bookFormat.toLowerCase());
            res = state.executeQuery(sql);
            if (res.next()) {
                int id = res.getInt("id");
                if (id > 0)
                    return id;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                if (res != null)
                    res.close();
                if (state != null)
                    state.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return 0;
    }

    public static Map<String, ScanInfoModel.ScanInfo> getScanInfo(String site) {
        Statement state = null;
        ResultSet res = null;
        try {
            state = DatabaseUtils.getConn(BookConstant.DATA_BASE).createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = String.format("SELECT cateInfo FROM scan_info where site = '%s' order by insertTime DESC limit 1", site);
            res = state.executeQuery(sql);
            if (res.next()) {
                String scanInfo = res.getString(1);
                if (StrUtils.isEmpty(scanInfo))
                    return null;

                D.i("info==>" + scanInfo);
                Type founderSetType = new TypeToken<Map<String, ScanInfoModel.ScanInfo>>() {
                }.getType();
                return gson.fromJson(scanInfo, founderSetType);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                if (res != null)
                    res.close();
                if (state != null)
                    state.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return null;
    }

    public static boolean setScanInfo(ScanInfoModel infoModel) {
        String sql = "insert into scan_info(insertTime, site, bookAdd, cateInfo, duration) values(?,?,?,?,?);";
        PreparedStatement state = null;

        try {
            state = DatabaseUtils.getConn(BookConstant.DATA_BASE).prepareStatement(sql);

            String info = gson.toJson(infoModel.cateInfo);
            state.setTimestamp(1, getTimestamp());
            state.setString(2, infoModel.site);
            state.setInt(3, infoModel.bookAdd.get());
            state.setString(4, info);
            state.setInt(5, infoModel.duration);

            int ret = state.executeUpdate();
            if (ret > 0)
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (state != null) {
                try {
                    state.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

        return false;
    }

    public static boolean testSaveSiteInfo(String site) {
        if (testSiteInfo(site))
            return false;

        if (!saveSiteInfo(site)) {
            D.e("保存历史访问url记录失败 ==>" + site);
        }
        return true;
    }

    public static boolean saveSiteInfo(String site) {
        String sql = "insert into repetition(url, md5, insertTime) values(?,?,?);";
        PreparedStatement state = null;

        try {
            state = DatabaseUtils.getConn(BookConstant.DATA_BASE).prepareStatement(sql);

            state.setString(1, site);
            state.setString(2, MD5Utils.strToMD5(site));
            state.setTimestamp(3, getTimestamp());

            int ret = state.executeUpdate();
            if (ret > 0)
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (state != null) {
                try {
                    state.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

        return false;
    }

    public static boolean testSiteInfo(String site) {
        Statement state = null;
        ResultSet res = null;
        try {
            state = DatabaseUtils.getConn(BookConstant.DATA_BASE).createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = String.format("SELECT url FROM repetition where md5 = '%s'", MD5Utils.strToMD5(site));
            res = state.executeQuery(sql);
            if (res.next()) {
                String scanInfo = res.getString(1);
                if (StrUtils.isEmpty(scanInfo))
                    return false;

                D.i("info==>" + scanInfo);
                if (scanInfo.equals(site))
                    return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                if (res != null)
                    res.close();
                if (state != null)
                    state.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return false;
    }

    public static Timestamp getTimestamp() {
        Date date = new Date(System.currentTimeMillis());//获得系统时间.
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        Timestamp timestamp = Timestamp.valueOf(nowTime);
        return timestamp;
    }
}
