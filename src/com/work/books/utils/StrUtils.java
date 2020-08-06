package com.work.books.utils;

import com.company.core.utils.D;

public class StrUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String subStr(String str, String subStart, String subEnd, boolean first) {
        int start = first ? str.indexOf(subStart) : str.lastIndexOf(subStart);
        int end = first ? str.indexOf(subEnd) : str.lastIndexOf(subEnd);
        String res = null;
        try {
            res = str.substring(start + subStart.length(), end);
        } catch (RuntimeException e) {
            D.e(String.format("subStr==>%s:%d  %s:%d  str:%s", subStart, start, subEnd, end, str));
            e.printStackTrace();
        }
        return res;
    }
}
