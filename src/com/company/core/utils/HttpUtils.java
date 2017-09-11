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
    private final static boolean DEBUG = true;
    public final static String POST = "POST";
    public final static String GET = "GET";
    //最大重试次数
    public final static int RETRY = 2;

    public static String getText(String url, Map<String,String> headers, Map<String,String> params, String kind){
        return null;
    }

    public static String getText(TaskModel task){
        if(task==null || task.url==null || task.url.trim().length()<7) {
            return null;
        }
        int responseCode;

        if(task.kind == null)
            if (task.params!=null && task.params.size()>0)
                task.kind = POST;
            else
                task.kind = GET;

        URLConnection rulConnection = null;
        try {
            URL url = new URL(task.url);
            if(task.status == Config.PROXY) {
                task.proxy = ProxyUtils.getProxy();
                task.proxy.useCount++;
                task.proxy.curr = System.currentTimeMillis();
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(task.proxy.kuaiModel.ip, task.proxy.kuaiModel.port));
                rulConnection = url.openConnection(proxy);
            } else
                rulConnection = url.openConnection();

            HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection;
            httpUrlConnection.setRequestMethod(task.kind);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setConnectTimeout(10000);
            httpUrlConnection.setReadTimeout(5000);

            if(task.headers!=null && task.headers.size()>0)
                for (Map.Entry<String,String> entry:task.headers.entrySet())
                    httpUrlConnection.setRequestProperty(entry.getKey(), entry.getValue());


            try {
                httpUrlConnection.connect();
                responseCode = httpUrlConnection.getResponseCode();
                if(responseCode!=200 && responseCode!=521) {
                    if(task.reTryConnCount > RETRY) {
                        task.errMsg = "HttpUtils  getText  httpUrlConnection.getResponseCode()"
                                +"\nretry connect greater than RETRY and response code is "+httpUrlConnection.getResponseCode();
                        task.app.failed(task);
                        return null;
                    }

                    //TODO 测试
                    System.out.println(httpUrlConnection.getResponseCode()+"  response err  "+task.url);

                    task.reTryConnCount++;
                    task.app.addHttpTask(task);

                    return null;
                }
            }catch (SocketTimeoutException ext){
                if(task.reTryConnCount>RETRY) {
                    task.errMsg = "HttpUtils  getText connect() or getResponseCode() retry connect greater than RETRY"
                            +"\n and catch a exception ==>" + ext.getMessage();
                    task.app.failed(task);
                    return null;
                }

                //TODO 测试
//                timeoutTest(task, "HttpUtils  getText connect() or getResponseCode()");
                if(DEBUG)
                    System.out.println(ext.toString()+"  connect()  "+task.url);

                task.reTryConnCount++;
                task.app.addHttpTask(task);

                return null;
            }

            if(task.params!=null && task.params.size()>0) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : task.params.entrySet())
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                sb.deleteCharAt(sb.length()-1);

                PrintWriter printWriter = new PrintWriter(httpUrlConnection.getOutputStream());
                printWriter.write(sb.toString());
                printWriter.flush();
                printWriter.close();
            }

            InputStream inputStream = null;
            if(responseCode == 200)
                inputStream = httpUrlConnection.getInputStream();
            else
                inputStream = httpUrlConnection.getErrorStream();

            //判断gzip
            String encoding = httpUrlConnection.getContentEncoding();
            if(encoding!=null && encoding.contains("gzip"))
                inputStream = new GZIPInputStream(inputStream);

            BufferedInputStream bis = new BufferedInputStream(inputStream);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int len;
            byte[] arr = new byte[1024];
            try {
                while((len=bis.read(arr))!= -1) {
                    bos.write(arr,0,len);
                    bos.flush();
                }
            } catch (IOException ext) {
                if(task.reTryReadCount > RETRY) {
                    task.errMsg = "HttpUtils  getText bis.read() retry read greater than RETRY"
                            +"\n and catch a exception ==>" + ext.getMessage();
                    task.app.failed(task);
                    return null;
                }
                //TODO 测试
                if(DEBUG)
                System.out.println(ext.toString()+"  bos.write  "+task.url);

                task.reTryReadCount++;
                task.app.addHttpTask(task);

                return null;
            } finally {
                bis.close();
            }

            String result = bos.toString("UTF-8");
            proxySucc(task);
            return result;
        } catch (IOException e) {
            task.proxy = null;

            task.errMsg = "HttpUtils  getText catch an exception ==>" + e.getMessage();
            task.app.failed(task);
            task.app.addHttpTask(task);
        }

        return null;
    }

    private static void proxySucc(TaskModel task){
        if(task!=null && task.proxy!=null) {
            task.proxy.ms = (int)((System.currentTimeMillis() - task.proxy.curr)+task.proxy.ms)/2;
            ++task.proxy.success;
        }
    }

    private static void timeoutTest(TaskModel task, String str) {
        if(task.reTryReadCount>3) {
            System.out.println(str+"  read==>"+ task.reTryReadCount +"   conn==>"+task.reTryConnCount+"--"+task.url);
        }

    }

    public static String getTextPost(String url, Map<String,String> headers, Map<String,String> params){
        return getText(url, headers, params, POST);
    }

    public static String getTextPost(String url, Map<String,String> params){
        return getText(url, null, params, POST);
    }

    public static String getTextGet(String url, Map<String,String> headers, Map<String,String> params){
        if(params!=null && params.size()>0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet())
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            sb.deleteCharAt(sb.length()-1);

            url += sb.toString();
        }

        return getText(url, headers, null, GET);
    }

    public static String getTextGet(String url, Map<String,String> params){
        return getText(url, null, params, GET);
    }

    public static String getTextGet(String url){
        return getText(url, null, null, GET);
    }

    public static void getResult(TaskModel task) {
        task.result = getText(task);

        if(task.result == null)
            return;

        task.app.addParseTask(task);
    }
}
