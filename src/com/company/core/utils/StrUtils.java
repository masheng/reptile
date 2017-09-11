package com.company.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ms on 2017/9/11.
 */
public class StrUtils {

    public static String getMatch(String content, String pattern, int index) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(content);

        if (matcher.find()) {
            return matcher.group(index);
        }

        return null;
    }

    /**
     * @return true str为空
     * */
    public static boolean isEmpty(String str) {
        if(str==null || str.length()==0)
            return true;
        return false;
    }
}
