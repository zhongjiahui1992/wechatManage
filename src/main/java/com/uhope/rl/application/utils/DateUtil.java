package com.uhope.rl.application.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by phl on 15-5-4.
 */
public final class DateUtil {
    private DateUtil(){

    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat();


    public static final String DATE_MMddHHmmss = "MM-dd HH:mm:ss";


    public static final String DATE_yyyyMMddHHmmss = "yyyyMMddHHmmss";


    public static final String DATE_yyyyMMddHHmm = "yyyyMMddHHmm";


    public static final String DATE_yyyyMMddHH = "yyyyMMddHH";


    public static final String DATE_yyyyMMdd = "yyyyMMdd";


    public static final String DATE_yyyyMM = "yyyyMM";


    public static final String DATE_yyyyMMdd_china = "yyyy年MM月dd日";

    public static final String DATE_yyyyMMddHHmmss_china = "yyyy年MM月dd日 HH时mm分ss秒";


    public static final String DATE_yyyyMMdd_E_china = "yyyy年MM月dd日 E";


    public static final String DATE_yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";


    public static final String DATE_yyyy_MM_dd_HH_mm = "yyyy-MM-dd HH:mm";


    public static final String DATE_yyyy_MM_dd_HH = "yyyy-MM-dd HH";


    public static final String DATE_yyyy_MM_dd = "yyyy-MM-dd";


    public static final String DATE_yyyy_MM_dd1 = "yyyy/MM/dd";


    public static final String DATE_EEEE = "EEEE";

    /**
     * 格式化日期
     *
     * @param date    日期
     * @param pattern 格式
     * @return 日期字符串
     */
    public static String formatDate(Date date, String pattern) {
        sdf.applyPattern(pattern);
        try {
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 格式化日期
     * @param time 秒数
     * @param pattern 格式
     * @return 日期字符串
     */
    public static String formatDate(long time, String pattern) {
        sdf.applyPattern(pattern);
        try {
            return sdf.format(new Date(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析日期
     *
     * @param date    日期字符串
     * @param pattern 格式
     * @return 日期
     */
    public static Date parseDate(String date, String pattern) {
        sdf.applyPattern(pattern);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Long TimeDiffMin(Date pBeginTime, Date pEndTime) {
        Long beginL = pBeginTime.getTime();
        Long endL = pEndTime.getTime();
        Long min = ((endL - beginL) % 86400000 % 3600000) / 60000;
        return min;
    }
}
