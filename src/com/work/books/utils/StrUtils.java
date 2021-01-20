package com.work.books.utils;

import com.company.core.utils.D;

public class StrUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 提取子串
     *
     * @param first true indexOf  false lastIndexOf
     */
    public static String subStr(String str, String subStart, String subEnd, boolean first) {
        if (!str.contains(subStart) || !str.contains(subEnd)) {
            D.w(String.format("subStr==>subStart:%s  subEnd:%s  str:%s", subStart, subEnd, str));
//            throw new RuntimeException("==>subStr");
            return str;
        }

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
