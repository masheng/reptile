//package com.work.app.app360;
//
//import com.company.Config;
//import com.company.core.utils.D;
//import com.work.app.app360.Model.App360InfoModel;
//import com.work.app.app360.Model.App360Model;
//import com.company.core.App;
//import com.company.core.IParse;
//import com.company.core.STEP;
//import com.company.core.TaskModel;
//import com.company.core.utils.DatabaseUtils;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.lang.reflect.Type;
//import java.sql.Date;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.StringTokenizer;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Created by ms on 2017/7/31.
// */
//
//public class App360 extends App implements IParse {
////    public static Logger log360 = LogManager.getLogger(App360.class);
//    //将时间转换为sql格式
//    Date insertTime= new java.sql.Date(new java.util.Date().getTime());
//    //直接使用桶存储 用于维护不同种类app的页数
//    private AtomicInteger pageIndex[] = new AtomicInteger[100];
//    //如果超过5次返回空页面，表示以后的页面已经没有内容
//    private AtomicInteger nullIndex[] = new AtomicInteger[100];
//
//    @Override
//    protected void firstPage() {
//        for (int i = 0; i < pageIndex.length; i++) {
//            pageIndex[i] = new AtomicInteger(0);
//            nullIndex[i] = new AtomicInteger(0);
//        }
//
//        //组装首页
//        TaskModel task = new TaskModel();
//        task.url = "http://app.so.com/category/";
//        task.app = this;
//        task.step = STEP.first;
//        addHttpTask(task);
//    }
//
//    @Override
//    public void parse(TaskModel task) {
//        switch (task.step) {
//            case first:
//                getFirstPageData(task);
//                break;
//            case second:
//                getInfo((App360Model) task);
//                break;
//            case third:
//                getInfoAll((App360InfoModel) task);
//                break;
//        }
//    }
//
//    @Override
//    public void failed(TaskModel taskModel) {
////        log360.error(taskModel.toString());
//    }
//
//    public void getFirstPageData(TaskModel task) {
//        if(task.result == null || task.result.length()<10) {
//            //TODO
//            return;
//        }
//
//        Document doc = Jsoup.parse(task.result);
//        Elements ul = doc.getElementsByClass("lists-soft");
//        Element ul_content = ul.get(0);
//        Elements li = ul_content.getElementsByTag("li");
//        for (Element data: li) {
//            String href = data.attr("data-href");
//
//            StringTokenizer st = new StringTokenizer(href, "&");
//
//            App360Model fd = new App360Model();
//
//            while(st.hasMoreElements()){
//                String token = st.nextToken();
//
//                if(token.startsWith("total")) {
//                    int index = token.indexOf("=");
//                    String value = token.substring(index+1, token.length());
//                    fd.total = Integer.parseInt(value);
//                } else if(token.startsWith("csid")) {
//                    int index = token.indexOf("=");
//                    String str = token.substring(index+1, token.length());
//                    if(str!=null && str.length()>0)
//                        fd.csid = Integer.valueOf(str);
//                } else if(token.startsWith("cat_name")){
//                    int index = token.indexOf("=");
//                    fd.cat_name = token.substring(index+1, token.length());
//                } else if(token.contains("cid=")){
//                    int index = token.indexOf("=");
//                    fd.cid = token.substring(index+1, token.length());
//                }
//            }
//
//            fd.app = this;
//            fd.step = STEP.second;
//
//            pageInc(fd);
//        }
//    }
//
//    //根据下标 获取第几页的数据 需要同步操作
//    private void pageInc(App360Model task){
//        if(task.csid == 0) {
//            System.out.println("err csid is 0");
//            return;
//        }
//
//        int index = task.csid>100?task.csid%100:task.csid;
//        task.index = pageIndex[index].incrementAndGet();
//        if(task.index > (task.total+10)/20)
//            return;
//
//        task.url = String.format("http://app.so.com/category/cat_request?page=%d&requestType=ajax&_t=%d&cid=%s&csid=%s&order=download", task.index, System.currentTimeMillis(), task.cid, task.csid);
//        addHttpTask(task);
//    }
//
//    private void getInfo(App360Model task) {
//        if(task.result.length() < 10)
//            System.out.println(task.csid+"==>getInfo resul is empty");
//
//        //如果有5个页面返回空 表示后面已经没有内容
//        int count = nullIndex[task.csid%100].get();
//        if(count < 7)
//            pageInc((App360Model)task);
//
//        if(task.result.length() < 10) {
//            if(task.status==Config.SUCCESS)
//                nullIndex[task.csid%100].incrementAndGet();
//
//            return;
//        }
//
//        Type type = new TypeToken<ArrayList<App360InfoModel>>(){}.getType();
//        ArrayList<App360InfoModel> jsonObjects = new Gson().fromJson(task.result, type);
//
//        if(jsonObjects==null) {
////            App360.log360.error("App360  getInfo  jsonObjects is null==>" + task.url+"--\n--"+task);
//            return;
//        }
//
//        if(jsonObjects.size()>0)
//            for (App360InfoModel data:jsonObjects) {
//                data.url = "http://app.so.com/detail/index?pname="+data.apkid+"&id="+data.id;
//                data.app = this;
//                data.step = STEP.third;
//                addHttpTask(data);
//            }
//    }
//
//    int count = 0;
//    private void getInfoAll(App360InfoModel data) {
//            Document docInfo = Jsoup.parse(data.result);
//            Element info = docInfo.getElementsByClass("software-form").get(0);
//            Element ulTag = info.getElementsByTag("ul").get(0);
//            Elements infoData = ulTag.getElementsByTag("li");
//            for (Element data1: infoData) {
//                String text = data1.text();
//                if(text.startsWith("更新")) {
//                    String update = text.substring(3, text.length());
//                    if(update!=null && update.length()>5)
//                        data.lastUpdate = Date.valueOf(update);
//                } else if(text.startsWith("作者")) {
//                    data.writer= text.substring(3, text.length());
//                }
//            }
//
//            Element descTag = docInfo.getElementById("fullDesc");
//            if(descTag != null) {
//                String desc = descTag.text();
//                data.desc = desc.replace("<p>.*</p>", "");
//            }
//
//            String sql = "insert into app360(name, download_times, lastUpdate, category_name, info, rating_fixed, writer, version_name, size, size_fixed, os_version, apkid, apid, insertTime) " +
//                    "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//
//            try {
//                PreparedStatement state = DatabaseUtils.getConn(DatabaseUtils.APP).prepareStatement(sql);
//                state.setString(1, data.name);
//                state.setInt(2, data.download_times);
//                state.setDate(3, data.lastUpdate);
//                state.setString(4, data.category_name);
//                state.setString(5, data.desc);
//                state.setInt(6, data.rating_fixed);
//                state.setString(7, data.writer);
//                state.setString(8, data.version_name);
//                state.setInt(9, data.size);
//                state.setFloat(10, data.size_fixed);
//                state.setInt(11, data.os_version);
//                state.setString(12, data.apkid);
//                state.setString(13, data.id);
//                state.setDate(14, insertTime);
//
//                count += state.executeUpdate();
//                if(count%1000==0)
//                    D.i("inserted==>"+count);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//    }
//}
