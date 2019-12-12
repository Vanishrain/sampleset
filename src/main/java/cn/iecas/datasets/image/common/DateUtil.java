package cn.iecas.datasets.image.common;


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
}
