package com.xuzp.common.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * @author za-xuzhiping
 * @Date 2018/7/26
 * @Time 16:52
 */
public class DateTimeUtils {

    public static String parseDate(String timemillis) {
        return DateFormatUtils.format(Long.parseLong(timemillis),
                "yyyy-MM-dd hh:mm:ss");
    }
}
