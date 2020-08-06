package com.company.core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by ms on 2017/9/11.
 */
public class D {
    public static final boolean DEBUG = true;
    public static Logger log = LogManager.getLogger();

    public static void p(String str) {
        System.out.println(str);
//        log.debug(str);
    }

    public static void w(String str) {
        System.err.println(str);
//        log.error(str);
    }

    public static void e(String str) {
        System.err.println(str);
//        log.error(str);
    }
}
