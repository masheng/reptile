package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.company.core.utils.FileUtils;
import com.work.books.utils.BookConstant;
import com.work.books.utils.DownModel;
import com.work.books.utils.InfoModel;
import com.work.books.utils.StrUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//http://panghubook.cn
//类别和类别id对应不上 解析使用json
public class PGApp extends BookAppTemp {
    private static final String PG_JSON = "reptile/src/com/work/books/apps/file/pg.json";
    private static final String TYPAN = "cloud.189";
    private static final String CTFILE = "ctfile";

    public static void main(String[] args) {
        PGApp app = new PGApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();

        //tag与类别对应 存放在app.js中
        String pgJson = FileUtils.getStr(PG_JSON);
        JSONArray arr = new JSONArray(pgJson);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String cate = obj.getString("name");
            int tag = obj.getInt("tag");
//	http://panghubook.cn/api/books/?p=2&tag=0
            TaskModel taskModel = createTask(CATEGORY);
            taskModel.url = String.format("http://panghubook.cn/api/books/?tag=%d&p=1", tag);
            taskModel.cate = cate;
            taskModel.obj = tag + "";
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseHome(TaskModel task) {

    }

    @Override
    protected void parseCate(TaskModel task) {
        parseList(task);

        JSONObject obj = new JSONObject(task.response);
        JSONObject data = obj.getJSONObject("data");
        int count = data.getInt("count");

        count = count / 10 + count % 10 == 0 ? 0 : 1;
        count = cateCount(task.cate, task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(LIST);
            taskModel.url = String.format("http://panghubook.cn/api/books/?tag=%d&p=%d", Integer.parseInt(task.obj), i);
            taskModel.cate = task.cate;
            taskModel.delayTime = 800;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {
        JSONObject obj = new JSONObject(task.response);
        JSONObject data = obj.getJSONObject("data");
        JSONArray results = data.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject item = results.getJSONObject(i);
            String url = "http://panghubook.cn/api/book/" + item.getString("id") + "/";

            InfoModel infoModel = new InfoModel();
            infoModel.pageUrl = url;
            infoModel.bookName = item.optString("title");
            if (infoModel.bookName.contains("《"))
                infoModel.bookName = StrUtils.subStr(infoModel.bookName, "《", "》", true);
            infoModel.bookFormat = item.optString("file_format");
            if (infoModel.bookFormat.contains(",")) {
                infoModel.bookFormat = infoModel.bookFormat.trim().replaceAll(",", "/");
                if (infoModel.bookFormat.contains(" "))
                    infoModel.bookFormat = infoModel.bookFormat.replaceAll(" ", "");
            }
            infoModel.bookAuthor = item.getString("author");
            if (infoModel.bookAuthor.contains("（作者）"))
                infoModel.bookAuthor = infoModel.bookAuthor.replace("（作者）", "");
            infoModel.bookImg = item.getString("img_url");
            infoModel.bookDesc = item.getString("desc");

            TaskModel taskModel = createTask(INFO);
            taskModel.url = url;
            taskModel.infoModel = infoModel;
            taskModel.testSite = true;
            taskModel.delayTime = 1000;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseInfo(TaskModel task) {
        JSONObject obj = new JSONObject(task.response).getJSONObject("data");
        String downs = obj.getString("download_url");
        String codes = obj.getString("download_pwd");
        String[] ds = downs.split(",");

        //解析提取码
        Map<Integer, String> map = null;
        if (!StrUtils.isEmpty(codes)) {
            String[] codeList = codes.split("  ");
            if (codeList != null && codeList.length > 0) {
                map = new HashMap<>();
                for (String c : codeList) {
                    String code = c.trim();
                    if (StrUtils.isEmpty(code) || code.length() < 4)
                        continue;

                    code = code.substring(code.indexOf("密码：") + "密码：".length());
                    if (c.contains("百度")) {
                        map.put(BookConstant.BAIDU_PAN, code);
                    } else if (c.contains("天翼"))
                        map.put(BookConstant.TY_PAN, code);
                }
            }
        }

        //解析下载地址
        for (String dl : ds) {
            String url = dl.trim();
            if (StrUtils.isEmpty(url))
                continue;

            int type = BookConstant.PRIVATE_PAN;
            if (url.contains(TYPAN))
                type = BookConstant.TY_PAN;
            else if (BookConstant.isBaiduPan(url))
                type = BookConstant.BAIDU_PAN;
            else if (url.contains(CTFILE))
                type = BookConstant.CTFILE_PAN;

            String code = "";
            if (map != null && !map.isEmpty()) {
                code = map.get(type);
                if (StrUtils.isEmpty(code))
                    code = "";
            }

            task.infoModel.addDownModel(new DownModel(url, code, type));
        }

        D.i("==>" + task.infoModel.toString());

        saveBook(task.infoModel);
    }
}
