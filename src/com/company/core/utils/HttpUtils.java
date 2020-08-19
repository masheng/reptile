package com.company.core.utils;

import com.company.core.model.TaskModel;
import org.jsoup.Jsoup;

import java.io.FileInputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by ms on 2017/7/31.
 */
public class HttpUtils {
    //请求网页 返回空页面超过VERIFY时，后续的请求将被忽略
    public final static int VERIFY = 3;
    private final static boolean DEBUG = true;
    public final static String POST = "POST";
    public final static String GET = "GET";

    /**
     * 处理网络请求
     */
    public static String disposeTask(TaskModel task) {
        if (task == null || task.url == null || task.url.trim().length() < 7) {
            return null;
        }
        int responseCode;

        if (!task.app.check(task))
            return null;

        if (task.delayTime > 0) {
            try {
                Thread.currentThread().sleep(task.delayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (task.requestType == null)
            if (task.params != null && task.params.size() > 0)
                task.requestType = POST;
            else
                task.requestType = GET;

        HttpURLConnection httpUrlConnection = null;

        try {
            //可以解决https问题
            URL url = new URL(encodeUrl(task.url));
            httpUrlConnection = (HttpURLConnection) url.openConnection();

            if (task.redirect)
                httpUrlConnection.setInstanceFollowRedirects(false);
            httpUrlConnection.setRequestMethod(task.requestType);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setConnectTimeout(70000);
            httpUrlConnection.setReadTimeout(8000);

            if (task.headers != null && task.headers.size() > 0)
                for (Map.Entry<String, String> entry : task.headers.entrySet())
                    httpUrlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            else
                httpUrlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.4 Safari/605.1.15");

            try {
                httpUrlConnection.connect();

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

                responseCode = httpUrlConnection.getResponseCode();
                //TODO 需要考虑重定向
                switch (responseCode) {
                    case 200:

                        break;
                    case 302:
                        task.redirectUrl = httpUrlConnection.getHeaderField("Location");
                        break;
                    case 403:
                    case 404:
                        task.errMsg = "response code is " + responseCode;
                        task.errCode = ErrorCode.RESPONSE_CODE_ERR;
                        task.app.onFailed(task);
                        return null;
                    default:
                        if (task.reTryConnCount > task.reTryMaxCount) {
                            task.errMsg = "HttpUtils  getText  httpUrlConnection.getResponseCode()"
                                    + "\nretry connect greater than RETRY and response code is " + httpUrlConnection.getResponseCode();
                            task.errCode = ErrorCode.RESPONSE_CODE_ERR;
                            task.app.onFailed(task);
                            return null;
                        }

                        //TODO 测试
                        System.out.println("连接失败 responseCode==>" + responseCode + "  重试次数" + task.reTryConnCount + "  response err==>" + task.url);

                        task.reTryConnCount++;
                        task.app.addHttpTask(task);
                        return null;
                }

            } catch (SocketTimeoutException ext) {
                if (task.reTryConnCount > task.reTryMaxCount) {
                    task.errMsg = "HttpUtils  getText connect() or getResponseCode() retry connect greater than RETRY"
                            + "\n and catch a exception ==>" + ext.getMessage();
                    task.errCode = ErrorCode.CONN_ERR;
                    task.app.onFailed(task);
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
                if (task.reTryReadCount > task.reTryMaxCount) {
                    task.errMsg = "HttpUtils  getText bis.read() retry read greater than RETRY"
                            + "\n and catch a exception ==>" + ext.getMessage();
                    task.errCode = ErrorCode.READ_ERR;
                    task.app.onFailed(task);
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

            String response = bos.toString(task.resEncode);
//            if (!StrUtils.isEmpty(task.resEncode) && !TaskModel.UTF8.equals(task.resEncode.toLowerCase())) {
//                response = new String(response.getBytes(task.resEncode), TaskModel.UTF8);
//            }

//            D.i("response==>" + response);

            if (task.parse)
                task.resDoc = Jsoup.parse(response, TaskModel.UTF8);
            task.response = response;
            task.app.parse(task);
            return response;
        } catch (IOException e) {
            task.errMsg = "HttpUtils  getText catch an exception ==>" + e.getLocalizedMessage();
            task.errCode = ErrorCode.READ_ERR;
            task.app.onFailed(task);
//            task.app.addHttpTask(task);
        }

        return null;
    }

    private static String encodeUrl(String url) {
        try {
            URL u = new URL(url);
            return encodeUrl(u).toExternalForm();
        } catch (Exception var2) {
            return url;
        }
    }

    private static URL encodeUrl(URL u) {
        try {
            String urlS = u.toExternalForm();
            urlS = urlS.replace(" ", "%20");
            URI uri = new URI(urlS);
            return new URL(uri.toASCIIString());
        } catch (MalformedURLException | URISyntaxException var3) {
            return u;
        }
    }
}
