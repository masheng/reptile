package com.work.books.apps;

import com.company.core.model.TaskModel;
import com.company.core.utils.D;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.work.books.utils.DownModel;
import com.work.books.utils.InfoModel;

public class QLXApp extends BookAppTemp {
    public static void main(String[] args) {
        QLXApp app = new QLXApp();
        app.startSingle();
    }

    @Override
    protected void config() {
        super.config();
        TaskModel task = createTask(HOME);
        task.url = "http://lxqnsys.com/pdf/php/getbooklist.php?channelid=all&pagenum=1&sorttype=new";
        task.parse = false;
        addHttpTask(task);
    }

    @Override
    protected void parseHome(TaskModel task) {
//        D.ee("==>" + task.response);
        parseInfo(task);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(task.response);
        JsonArray rootArr = element.getAsJsonArray();
        JsonObject obj = rootArr.get(0).getAsJsonObject();
        int total = obj.get("total").getAsInt();
        int count = total / 16 + 1;

        count = cateCountDefault(task.url, count);

        for (int i = 2; i <= count; i++) {
            TaskModel taskModel = createTask(INFO);
            task.url = "http://lxqnsys.com/pdf/php/getbooklist.php?channelid=all&pagenum=" + i + "&sorttype=new";
            task.parse = false;
            addHttpTask(taskModel);

            if (D.DEBUG)
                break;
        }
    }

    @Override
    protected void parseList(TaskModel task) {

    }

    @Override
    protected void parseInfo(TaskModel task) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(task.response);
        JsonArray rootArr = element.getAsJsonArray();
        JsonObject objRoot = rootArr.get(1).getAsJsonObject();
        JsonArray arr = objRoot.getAsJsonArray("list");
        for (JsonElement e : arr) {
            JsonObject obj = e.getAsJsonObject();
            InfoModel infoModel = new InfoModel();
            infoModel.pageUrl = task.url;
            infoModel.bookName = obj.get("bookname").getAsString();
            infoModel.bookAuthor = obj.get("bookauthor").getAsString();
            infoModel.addDownModel(new DownModel(obj.get("bookurl").getAsString()));

            D.i("==>" + infoModel.toString());
            saveBook(infoModel);

            if (D.DEBUG)
                break;
        }
    }


}
