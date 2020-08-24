package com.work.books;

import com.company.core.utils.D;
import com.company.core.utils.ThreadUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) {
        File file = new File("./reptile/src/com/work/books/apps/");

        ArrayList<String> apps = new ArrayList<>();
        File dirs[] = file.listFiles();
        for (File f : dirs) {
            if (f.isFile() && f.getName().endsWith("App.java")) {
                String clsName = f.getName().substring(0, f.getName().length() - ".java".length());
                apps.add(clsName);
            }
        }

        CountDownLatch latch = new CountDownLatch(apps.size());

        for (String name : apps) {
            new Thread(() -> {
                work(name);
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        D.ee("==>main方法执行完成");
    }

    private static void work(String clsName) {
        try {
            Class cls = Class.forName("com.work.books.apps." + clsName);
            Object obj = cls.newInstance();
            Method main = cls.getDeclaredMethod("main", String[].class);
            String[] args = new String[]{""};
            main.invoke(obj, (Object) args);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}