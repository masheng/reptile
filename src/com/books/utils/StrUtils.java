package com.books.utils;

import com.company.core.utils.D;

public class StrUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String subStr(String str, String subStart, String subEnd, boolean first) {
        int start = first ? str.indexOf(subStart) : str.lastIndexOf(subStart);
        int end = first ? str.indexOf(subEnd) : str.lastIndexOf(subEnd);
        D.e(String.format("subStr==>%s:%d  %s:%d", subStart, start, subEnd, end));
        return str.substring(start + subStart.length(), end);
    }
}
