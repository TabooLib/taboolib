package me.skymc.taboolib.other;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     * 最后一次更新：2018年1月16日21:07:27
     *
     * @author sky
     */

    public static SimpleDateFormat CH_ALL = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
    public static SimpleDateFormat EN_ALL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static SimpleDateFormat YEAR = new SimpleDateFormat("yyyy");

    public static SimpleDateFormat MONTH = new SimpleDateFormat("MM");

    public static SimpleDateFormat DAY_OF_MONTH = new SimpleDateFormat("dd");
    public static SimpleDateFormat DAY_OF_YEAR = new SimpleDateFormat("DD");

    public static SimpleDateFormat HOUR_OF_DAY = new SimpleDateFormat("HH");
    public static SimpleDateFormat HOUR = new SimpleDateFormat("hh");

    public static SimpleDateFormat MINUTE = new SimpleDateFormat("mm");
    public static SimpleDateFormat SECONDS = new SimpleDateFormat("ss");

    public static SimpleDateFormat MILLISECOND = new SimpleDateFormat("SSS");

    public static SimpleDateFormat WEEK = new SimpleDateFormat("E");

    private static SimpleDateFormat Hour = new SimpleDateFormat("HH:mm");

    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    public static Date getTime() {
        return Calendar.getInstance().getTime();
    }

    public static Integer getTime(SimpleDateFormat date) {
        return Integer.valueOf(date.format(getTime()));
    }

    public static boolean timeInDifference(String m1, String m2) {
        try {
            Date now = Hour.parse(Hour.format(System.currentTimeMillis()));
            Date min = Hour.parse(m1);
            Date max = Hour.parse(m2);
            return (now.after(min) && now.before(max)) || now.equals(min) || now.equals(max);
        } catch (Exception e) {
            return false;
        }
    }

    public static long formatDate(String time) {
        long date = 0;
        try {
            for (String value : time.toLowerCase().split(";")) {
                Integer num = Integer.valueOf(value.substring(0, value.length() - 1));
                if (value.endsWith("d")) {
                    date += num * 1000L * 60 * 60 * 24;
                } else if (value.endsWith("h")) {
                    date += num * 1000L * 60 * 60;
                } else if (value.endsWith("m")) {
                    date += num * 1000L * 60;
                } else if (value.endsWith("s")) {
                    date += num * 1000L;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return date;
    }
}
