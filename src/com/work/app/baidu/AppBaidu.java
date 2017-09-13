package com.work.app.baidu;

import com.company.core.App;
import com.company.core.ICheckSend;
import com.company.core.STEP;
import com.company.core.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.DatabaseUtils;
import com.company.core.utils.HttpUtils;
import com.company.core.utils.StrUtils;
import com.google.gson.*;
import com.work.app.baidu.Model.BaiduInfo;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.company.core.utils.HttpUtils.GET;

/**
 * Created by ms on 2017/9/10.
 */

public class AppBaidu extends App implements ICheckSend{
    //类别
    final String cate[] = new String[50];
    //请求id
    final String boardid[] = new String[50];
    AtomicInteger pageIndex[] = new AtomicInteger[30];
//    AtomicInteger nullIndex[] = new AtomicInteger[30];
    volatile int nullIndex[] = new int[30];
//    AtomicInteger lastNullIndex[] = new AtomicInteger[30];
    //头全部相同
    private static final Map<String, String> headers = new HashMap<>();
    private static final String URL1 = "https://appc.baidu.com/uiserver?cen=cuid_cut_cua_uid&abi=armeabi-v7a&action=generalboard&province=qivtkjihetggRHi86iS3kliheug_MHf3odfqA&gms=true&catestrategy=adv&cll=ga24N_OqB8guue8r0a2YN_uSv8mEA&apn="
                                        + "&usertype=1&sorttype=soft&pkname=com.baidu.appsearch&native_api=1&disp=NXT-AL10C00B577&from=1000561u&cct=qivtkjihetggRHi86iS3kliheug_MHf3odfqA&pu=ctv%401%2Ccfrom%401000561u%2Ccua%40_a-qi4aqBig4NE65I5me6NNy2IYUhvCeSdNqA%2Ccuid%400a-QagPQ280KaHaUli2au_ugv8_SOviJg8Hm8_iO-i6WuviJMLh3C%2Ccut%405kSYMlfoXOynksa65avjh_h0vC_2uDPWpi3purohC%2Cosname%40baiduappsearch&network=WF&boardid=board_";
    //前面添加id
    private static final String URL2 = "&=&operator=460010&country=CN&psize=3&is_support_webp=true&subcateurl=true&uid=0a-QagPQ280KaHaUli2au_ugv8_SOviJg8Hm8_iO-iqDuHi3A&language=zh&platform_version_id=24&ver=16793738&&crid=1505213215046&native_api=1&pn=";
    //前面添加页数
    private static final String URL3 = "&f=soft%24%24%24soft%40cate%40from_launcher%40catesoft%401%401&bannert=26%4027%4028%4029%4030%4032%4043&ptl=hps";
//private static final String URL1 = "http://appc.baidu.com/uiserver?uid=0av68lPz28lVuBao0u-6i0iNStgfaS8Tg8vuiluqHiqjuSf3_avKt_a928gpavi03dDoC&=&abi=armeabi-v7a&subcateurl=true&is_support_webp=true&ver=16793302&from=1020184b&cct=qivtkjihetggRHi86iS3kliheug_MHf3odfqA&network=WF&catestrategy=adv&cen=cuid_cut_cua_uid&platform_version_id=19&province=qivtkjihetggRHi86iS3kliheug_MHf3odfqA&boardid=board_";
////            100_0001"
//private static final String URL2 = "&action=generalboard&apn=&native_api=1&sorttype=soft&psize=3&cll=ga24N_OqB8_Cue8r0a2YN_uSv8mEA&usertype=0&operator=460028&pkname=com.baidu.appsearch&country=CN&gms=false&pu=cua@_a-qi4uq-igBNE6lI5me6NNy2IYUCvhlSGNqA,osname@baiduappsearch,ctv@1,cfrom@1020184b,cuid@0av68lPz28lVuBao0u-6i0iNStgfaS8Tg8vuiluqHi6ouviJlP2kigPL28_Iuv8kga2Vtqq6B,cut@5fXROrktSNrvkShJ_h2UIgN0vtyNNmojipQmA&language=zh&&crid=1505298369004&native_api=1&pn=";
////页
//private static final String URL3 = "&f=soft$$$soft@cate@from_launcher@subcatesoft";
////    @1@1
//    private static final String URL4 = "&bannert=26@27@28@29@30@32@43&cpver=4&rqt=rty&ptl=hp";

    @Override
    public void parse(TaskModel taskModel) {
        switch (taskModel.step) {
            case first:
//                D.p("==>"+taskModel.result);
                firstParse(taskModel);
                break;
            case second:
                infoParse(taskModel);
                break;
        }
    }

    private void firstParse(TaskModel taskModel) {
        JsonParser parse =new JsonParser();
        JsonObject obj = (JsonObject) parse.parse(taskModel.result);
        JsonArray result = obj.getAsJsonObject("result").getAsJsonArray("data");
        List<String> type = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            JsonObject real = (JsonObject) result.get(i);

            if(real.get("datatype").getAsInt() == 21) {
                JsonObject itemdata = real.getAsJsonObject("itemdata");
                String dataurl = itemdata.get("dataurl").toString();
                String catename = itemdata.get("catename").toString();
                int pos = itemdata.get("pos").getAsInt();
                cate[pos] = catename;
                //"uiserver?native_api=1&action=generalboard&boardid=board_101_0310&subcateurl=true&sorttype=soft",
                //获取 101_0310
                int offset = 57;
                int index = dataurl.indexOf("&", offset);
                String rrr = new String(dataurl.toCharArray(), offset, index-offset);
                boardid[pos] = rrr;

                String startUrl = URL1 + rrr + URL2 + pageIndex[pos].getAndIncrement() +  URL3;
                TaskModel newTask = new TaskModel(startUrl, this, STEP.second);
                newTask.headers = headers;
                newTask.kind = GET;
                newTask.arg = pos;

                addHttpTask(newTask);
            }
        }
    }

    private void infoParse(TaskModel taskModel) {
        pageInc(taskModel);

        JsonParser parse =new JsonParser();
        JsonObject obj = (JsonObject) parse.parse(taskModel.result);
        int status = obj.get("error_no").getAsInt();
        if(status == 0) {
            JsonArray data = obj.getAsJsonObject("result").getAsJsonArray("data");
            for (int i = 0; i < data.size(); i++) {
                BaiduInfo info = new BaiduInfo();
                JsonObject itemdata = data.get(i).getAsJsonObject().getAsJsonObject("itemdata");
                JsonElement sname = itemdata.get("sname");
                if(sname == null)
                    continue;
                info.sname = sname.getAsString();
                JsonElement packageid = itemdata.get("packageid");
                info.packageid = packageid==null?"null":packageid.getAsString();
                JsonElement versionname = itemdata.get("versionname");
                info.versionname = versionname==null?"null":versionname.getAsString();
                info.versioncode = itemdata.get("versioncode").getAsString();
                info.cateid = itemdata.get("cateid").getAsString();
                info.size_ori = itemdata.get("size_ori").getAsString();
                String updatetime = itemdata.get("updatetime").getAsString();
                info.updatetime = StrUtils.isEmpty(updatetime)?"2030-1-1":updatetime;
                info.package_name = itemdata.get("package").getAsString();
                info.score = itemdata.get("score").getAsInt();
                info.score_count = itemdata.get("score_count").getAsInt();
                info.display_download = itemdata.get("display_download").getAsLong();
                String popularity = itemdata.get("popularity").getAsString();
                info.popularity = StrUtils.isEmpty(popularity)?-1:Integer.valueOf(popularity);
                info.platform_version = itemdata.get("platform_version").getAsString();
                info.manual_short_brief = itemdata.get("manual_short_brief").getAsString();
                info.brief = itemdata.get("brief").getAsString();
                String yesterday_download_pid = itemdata.get("yesterday_download_pid").getAsString();
                info.yesterday_download_pid = StrUtils.isEmpty(yesterday_download_pid)?-1:Integer.valueOf(yesterday_download_pid);
                info.category = cate[taskModel.arg];

                insertDatabase(info);
            }
        } else {
            //设置为空页面最小的值
            if(nullIndex[taskModel.arg]==0 || nullIndex[taskModel.arg]>taskModel.arg1)
                nullIndex[taskModel.arg]=taskModel.arg1;
        }
    }

    //根据下标 获取第几页的数据 需要同步操作
    private void pageInc(TaskModel task){
        if(nullIndex[task.arg] > 0)
            return;

        int index = pageIndex[task.arg].getAndIncrement();
        String url = URL1 + boardid[task.arg] + URL2 + index +  URL3;

        TaskModel newTask = new TaskModel(url, this, STEP.second);
        newTask.headers = headers;
        newTask.kind = GET;
        newTask.arg = task.arg;
        newTask.arg1 = index;

        addHttpTask(newTask);
    }

    private void insertDatabase(BaiduInfo data){

        String sql = "insert into baidu(sname, score, score_count, popularity, display_download, yesterday_download_pid, updatetime, " +
                "brief, manual_short_brief, cateid, category, " +
                "versionname, versioncode, size_ori, platform_version, package_name, packageid, insertTime) " +
                "values(?,?,?,?,?," +
                "?,?,?,?,?," +
                "?,?,?,?,?," +
                "?,?,?)";

        try {
            PreparedStatement state = DatabaseUtils.getConn(DatabaseUtils.APP).prepareStatement(sql);
            state.setString(1, data.sname);
            state.setFloat(2, data.score);
            state.setInt(3, data.score_count);
            state.setInt(4, data.popularity);
            state.setLong(5, data.display_download);
            state.setInt(6, data.yesterday_download_pid);
            state.setDate(7, Date.valueOf(data.updatetime));
            state.setString(8, data.brief);
            state.setString(9, data.manual_short_brief);
            state.setString(10, data.cateid);
            state.setString(11, data.category);
            state.setString(12, data.versionname);
            state.setString(13, data.versioncode);
            state.setString(14, data.size_ori);
            state.setString(15, data.platform_version);
            state.setString(16, data.package_name);
            state.setString(17, data.packageid);
            state.setDate(18, new java.sql.Date(new java.util.Date().getTime()));

            state.executeUpdate();
//            if(count%1000==0)
//                D.p("inserted==>"+count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(TaskModel taskModel) {

        D.p("err==>"+taskModel.errMsg);
    }

    @Override
    protected void firstPage() {
        String one ="https://appc.baidu.com/appsrv?cen=cuid_cut_cua_uid&abi=armeabi-v7a&action=boardcate&province=qivtkjihetggRHi86iS3kliheug_MHf3odfqA&gms=true&swapphoneflag=0&cll=ga24N_OqB8guue8r0a2YN_uSv8mEA&mspace=0.62623&apn=&usertype=1&pkgnum=11&sorttype=soft&pkname=com.baidu.appsearch&native_api=1&disp=NXT-AL10C00B577&from=1000561u&cct=qivtkjihetggRHi86iS3kliheug_MHf3odfqA&pu=ctv%401%2Ccfrom%401000561u%2Ccua%40_a-qi4aqBig4NE65I5me6NNy2IYUhvCeSdNqA%2Ccuid%400a-QagPQ280KaHaUli2au_ugv8_SOviJg8Hm8_iO-i6WuviJMLqSC%2Ccut%405kSYMlfoXOynksa65avjh_h0vC_2uDPWpi3purqPC%2Cosname%40baiduappsearch&network=WF&operator=460010&country=CN&psize=3&is_support_webp=true&uid=0a-QagPQ280KaHaUli2au_ugv8_SOviJg8Hm8_iO-iqDuHi3A&language=zh&platform_version_id=24&ver=16793738&&crid=1505211086268&native_api=1&pn=&f=soft%40cate%40from_launcher&bannert=26%4027%4028%4029%4030%4032%4043&ptl=hps";
        String s1 = "";

        for (int i = 0; i < pageIndex.length; i++) {
            pageIndex[i] = new AtomicInteger(0);
        }

        TaskModel taskModel=new TaskModel(one, this, STEP.first);
        headers.put("sign", s1);
        headers.put("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 7.0; HUAWEI NXT-AL10 Build/HUAWEINXT-AL10)");
        headers.put("Host", "appc.baidu.com");
        headers.put("Connection", "Keep-Alive");
        headers.put("Accept-Encoding", "gzip");
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        taskModel.headers = headers;
        taskModel.kind = GET;

        addHttpTask(taskModel);
    }

    @Override
    public boolean check(TaskModel task) {
        if(nullIndex[task.arg]!=0 && task.arg1>nullIndex[task.arg])
            return true;

        return false;
    }

}
