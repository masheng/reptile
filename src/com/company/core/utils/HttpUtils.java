package com.company.core.utils;

import com.company.Config;
import com.company.core.TaskModel;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by ms on 2017/7/31.
 */
public class HttpUtils {
    private final static boolean DEBUG = false;
    public final static String POST = "POST";
    public final static String GET = "GET";
    //最大重试次数
    public final static int RETRY = 5;

    public static String getText(TaskModel task) {
        if (task == null || task.url == null || task.url.trim().length() < 7) {
            return null;
        }
        int responseCode;

        if (task.kind == null)
            if (task.params != null && task.params.size() > 0)
                task.kind = POST;
            else
                task.kind = GET;

        URLConnection rulConnection = null;
        try {
            URL url = new URL(task.url);
            rulConnection = url.openConnection();

            HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection;
            httpUrlConnection.setRequestMethod(task.kind);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setConnectTimeout(10000);
            httpUrlConnection.setReadTimeout(8000);

            if (task.headers != null && task.headers.size() > 0)
                for (Map.Entry<String, String> entry : task.headers.entrySet())
                    httpUrlConnection.setRequestProperty(entry.getKey(), entry.getValue());


            try {
                httpUrlConnection.connect();
                responseCode = httpUrlConnection.getResponseCode();
                if (responseCode != 200) {
                    if (task.reTryConnCount > RETRY) {
                        task.errMsg = "HttpUtils  getText  httpUrlConnection.getResponseCode()"
                                + "\nretry connect greater than RETRY and response code is " + httpUrlConnection.getResponseCode();
                        task.app.failed(task);
                        return null;
                    }

                    //TODO 测试
                    System.out.println(httpUrlConnection.getResponseCode() + "  response err  " + task.url);

                    task.reTryConnCount++;
                    task.app.addHttpTask(task);

                    return null;
                }
            } catch (SocketTimeoutException ext) {
                if (task.reTryConnCount > RETRY) {
                    task.errMsg = "HttpUtils  getText connect() or getResponseCode() retry connect greater than RETRY"
                            + "\n and catch a exception ==>" + ext.getMessage();
                    task.app.failed(task);
                    return null;
                }

                //TODO 测试
//                timeoutTest(task, "HttpUtils  getText connect() or getResponseCode()");
                if (DEBUG)
                    System.out.println(ext.toString() + "  connect()  " + task.url);

                task.reTryConnCount++;
                task.app.addHttpTask(task);

                return null;
            }

            if (task.params != null && task.params.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : task.params.entrySet())
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                sb.deleteCharAt(sb.length() - 1);

                PrintWriter printWriter = new PrintWriter(httpUrlConnection.getOutputStream());
                printWriter.write(sb.toString());
                printWriter.flush();
                printWriter.close();
            }

            InputStream inputStream = httpUrlConnection.getInputStream();

            //判断gzip
            String encoding = httpUrlConnection.getContentEncoding();
            if (encoding != null && encoding.contains("gzip"))
                inputStream = new GZIPInputStream(inputStream);

            BufferedInputStream bis = new BufferedInputStream(inputStream);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int len;
            byte[] arr = new byte[1024];
            try {
                while ((len = bis.read(arr)) != -1) {
                    bos.write(arr, 0, len);
                    bos.flush();
                }
            } catch (IOException ext) {
                if (task.reTryReadCount > RETRY) {
                    task.errMsg = "HttpUtils  getText bis.read() retry read greater than RETRY"
                            + "\n and catch a exception ==>" + ext.getMessage();
                    task.app.failed(task);
                    return null;
                }
                //TODO 测试
                if (DEBUG)
                    System.out.println(ext.toString() + "  bos.write  " + task.url);

                task.reTryReadCount++;
                task.app.addHttpTask(task);

                return null;
            } finally {
                bis.close();
            }

            String result = bos.toString("UTF-8");
            return result;
        } catch (IOException e) {
            task.errMsg = "HttpUtils  getText catch an exception ==>" + e.getMessage();
            task.app.failed(task);
            task.app.addHttpTask(task);
        }

        return null;
    }

    public static void getResult(TaskModel task) {
        task.result = getText(task);

        if (task.result == null)
            return;

        task.app.addParseTask(task);
    }
}
