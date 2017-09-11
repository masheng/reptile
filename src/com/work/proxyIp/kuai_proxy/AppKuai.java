package com.work.proxyIp.kuai_proxy;

import com.company.Config;
import com.company.core.App;
import com.company.core.STEP;
import com.company.core.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.DatabaseUtils;
import com.company.core.utils.StrUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.script.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ms on 2017/9/10.
 */
//http://www.kuaidaili.com/free/
public class AppKuai extends App {
    private int count = 0;
    private static final String PATH = "http://www.kuaidaili.com/free/inha/";
    private AtomicInteger pageIndex = new AtomicInteger(1);

    private HashMap<String, String> headers = new HashMap<>();

    @Override
    public void parse(TaskModel taskModel) {
        switch (taskModel.step) {
            case first:
                parseCookie(taskModel);
                break;
            case second:
                realData(taskModel);
//                D.p("==>" + taskModel.result);
                break;
        }
    }

    int ccc=0;
    @Override
    public void failed(TaskModel taskModel) {
        if(taskModel.status != Config.PROXY) {
            taskModel.status = Config.PROXY;
            taskModel.reTryConnCount = taskModel.reTryReadCount = 0;
            addHttpTask(taskModel);
            D.p("failed==>"+ccc++);
            return;
        }

        D.p("==>"+taskModel.url);
    }

    @Override
    protected void firstPage() {
//        super.coreThread = 1;
//        super.maxThread = 10;
//        super.queueCap = 3000;
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/603.2.4 (KHTML, like Gecko) Version/10.1.1 Safari/603.2.4");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Request Version", "HTTP/1.1");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("Accept-Language", "zh-cn");
        headers.put("Host", "www.kuaidaili.com");

        for (int i = 0; i < 500; i++) {
            request();
        }

    }

    private void request(){
        TaskModel task = new KuaiModel(PATH + pageIndex.getAndIncrement(), this, STEP.first);
        task.headers = this.headers;

        addHttpTask(task);
    }

    //https://my.oschina.net/jhao104/blog/865966
    //TODO 出错处理
    private void parseCookie(TaskModel task) {
        if (!StrUtils.isEmpty(task.result)) {
            String jsFunction = StrUtils.getMatch(task.result, "function .*?\\);\"\\);}", 0);
            String param = StrUtils.getMatch(task.result, "setTimeout\\(\\\"\\D+\\((\\d+)\\)\\\"", 1);
            String paramName = StrUtils.getMatch(task.result, "function.*?\\(([a-zA-Z0-9]*)\\)", 1);
            String functionName = StrUtils.getMatch(task.result, "function.([a-zA-Z0-9]*)\\(", 1);
//            D.p(param+"--"+functionName);
            if (jsFunction != null && jsFunction.length() > 10) {
                jsFunction = jsFunction.replace("eval(\"qo=eval;qo(po);\")", "return po");
//                D.p(jsFunction);
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("javascript");

                try {
                    engine.eval(jsFunction);
                    Invocable invocable = (Invocable) engine;
                    String cookies = (String) invocable.invokeFunction(functionName, param);
                    cookies = StrUtils.getMatch(cookies, "\'(.*?)\'", 1);

                    if (StrUtils.isEmpty(cookies))
                        return;
                    task.headers.put("Cookie", cookies);
                    task.step = STEP.second;
                    addHttpTask(task);

                    return;
                } catch (ScriptException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        D.p("AppKuai    getCookie   error");
    }

    private void realData(TaskModel task) {
//        if(task.arg == 0)
//            reRequest((KuaiModel) task);
        List<KuaiModel> datas = new ArrayList<>();
        Document document = Jsoup.parse(task.result);
        Elements tables = document.getElementsByTag("table");
        for(Element table:tables) {
            Elements bodys = table.getElementsByTag("tbody");
            for (Element body : bodys) {
                Elements trs = body.getElementsByTag("tr");
                for (Element tr : trs) {
                    Elements tds = tr.getElementsByTag("td");
                    KuaiModel model = new KuaiModel();
                    for (Element td : tds) {
                        String tag = td.attr("data-title");
                        String dt = td.text();
                        switch (tag) {
                            case "IP":
                                model.ip = dt;
                                break;
                            case "PORT":
                                model.port = Integer.parseInt(dt);
                                break;
                            case "匿名度":
                                model.level = dt;
                                break;
                            case "类型":
                                model.type = dt;
                                break;
                            case "位置":
                                model.location = dt;
                                break;
                            case "响应速度":
                                dt = dt.substring(0, dt.length() - 1);
                                if(StrUtils.isEmpty(dt)) {
                                    D.p("响应速度==>"+td.text());
                                    break;
                                }

                                model.speed = (int) (Float.parseFloat(dt) * 1000);
                                break;
                            case "最后验证时间":
                                model.lastUpdate = dt;
                                break;
                        }
                    }
                    datas.add(model);
                }
            }
        }

        if(datas.size() > 0) {
            String sql = "insert into kuai_proxy(ip, port, grade, level, lastUpdate, speed, test, location, type, insertTime) " +
                    "values(?,?,?,?,?,?,?,?,?,?)";
            for(KuaiModel data:datas) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long time = 0;
                try {
                    time = format.parse(data.lastUpdate).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                PreparedStatement state = null;
                try {
                    state = DatabaseUtils.getConn(DatabaseUtils.PROXY).prepareStatement(sql);
                    state.setString(1, data.ip);
                    state.setInt(2, data.port);
                    state.setInt(3, data.grade);
                    state.setString(4, data.level);
                    state.setDate(5, new java.sql.Date(time));
                    state.setInt(6, data.speed);
                    state.setInt(7, data.test);
                    state.setString(8, data.location);
                    state.setString(9, data.type);
                    state.setDate(10, new java.sql.Date(System.currentTimeMillis()));

                    count += state.executeUpdate();
                    D.p("insert res ==>"+count);
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
//        D.p(datas.toString());
    }
    //获取透明代理
    private void reRequest(KuaiModel task){
//        task.url.replace("inha", "intr");
//        task.arg = 1;
//        task.reset();
//
//        addHttpTask(task);
    }
}