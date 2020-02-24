package cn.iecas.datasets.image.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 */
public class DateUtil {
    public static final String datatimePattern="yyyy-MM-dd";
    public static final String creatimePattern="yyyy-MM-dd HH:mm:ss ";

    /**
     * 字符串转日期
     * @param times
     * @return
     */
    public static Date fromStringToDate(String times,String pattern) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.parse(times);
    }

    //日期转字符串
    public static String fromDateToString(Date date, String pattern){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    //时间戳转字符串
    public static String fromLongToString(long time, String pattern){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(time);
    }
}
