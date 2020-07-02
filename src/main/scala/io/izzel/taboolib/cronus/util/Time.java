package io.izzel.taboolib.cronus.util;

import com.google.common.collect.Maps;
import io.izzel.taboolib.cronus.CronusUtils;
import org.bukkit.util.NumberConversions;

import java.util.Calendar;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-05-28 17:28
 */
public class Time {

    private static final Map<String, Time> cacheMap = Maps.newHashMap();
    private final TimeType type;
    private int day;
    private int hour;
    private int minute;
    private long time;
    private final Map<Long, Calendar> cacheEnd = Maps.newHashMap();
    private Calendar end;
    private String origin;

    public Time(String libTime) {
        this(CronusUtils.toMillis(libTime));
    }

    public Time(long time) {
        this.type = TimeType.TIME;
        this.time = time;
    }

    public Time(int hour, int minute) {
        this.type = TimeType.DAY;
        this.hour = hour;
        this.minute = minute;
    }

    public Time(TimeType type, int day, int hour, int minute) {
        this.type = type;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public Time origin(String origin) {
        this.origin = origin;
        return this;
    }

    public Time in(long start) {
        if (this.cacheEnd.containsKey(start)) {
            this.end = this.cacheEnd.get(start);
        } else {
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(start);
            this.end = startCal;
            this.end.set(Calendar.SECOND, 0);
            this.end.set(Calendar.MILLISECOND, 0);
            this.cacheEnd.put(start, this.end);
            if (this.type != TimeType.TIME) {
                switch (this.type) {
                    case DAY:
                        this.end.set(Calendar.HOUR_OF_DAY, hour);
                        this.end.set(Calendar.MINUTE, minute);
                        if (Calendar.getInstance().after(this.end)) {
                            this.end.add(Calendar.DATE, 1);
                        }
                        break;
                    case WEEK:
                        this.end.set(Calendar.DAY_OF_WEEK, day + 1);
                        this.end.set(Calendar.HOUR_OF_DAY, hour);
                        this.end.set(Calendar.MINUTE, minute);
                        if (Calendar.getInstance().after(this.end)) {
                            this.end.add(Calendar.DATE, 7);
                        }
                        break;
                    case MONTH:
                        this.end.set(Calendar.DAY_OF_MONTH, day);
                        this.end.set(Calendar.HOUR_OF_DAY, hour);
                        this.end.set(Calendar.MINUTE, minute);
                        if (Calendar.getInstance().after(this.end)) {
                            this.end.add(Calendar.MONTH, 1);
                        }
                        break;
                }
            }
        }
        return this;
    }

    public boolean isTimeout(long start) {
        return type == TimeType.TIME ? start + time < System.currentTimeMillis() : isTimeout();
    }

    public boolean isTimeout() {
        return Calendar.getInstance().after(this.end);
    }

    public boolean isEquals() {
        Calendar calendar = Calendar.getInstance();
        switch (type) {
            case DAY: {
                return calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) == minute;
            }
            case WEEK: {
                return calendar.get(Calendar.DAY_OF_WEEK) == day && calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) == minute;
            }
            case MONTH: {
                return calendar.get(Calendar.DAY_OF_MONTH) == day && calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) == minute;
            }
            default:
                return false;
        }
    }

    public static Time parse(String in) {
        return cacheMap.computeIfAbsent(in, n -> parse0(in));
    }

    public static Time parse0(String in) {
        if (in == null) {
            return null;
        }
        in = in.toLowerCase();
        if (in.equalsIgnoreCase("never") || in.equals("-1")) {
            return null;
        } else if (in.startsWith("day:")) {
            String[] v = in.substring("day:".length()).split(":");
            return new Time(NumberConversions.toInt(v[0]), NumberConversions.toInt(v.length > 1 ? v[1] : 0)).origin(in);
        } else if (in.startsWith("week:")) {
            String[] v = in.substring("week:".length()).split(":");
            return new Time(TimeType.WEEK, NumberConversions.toInt(v[0]), NumberConversions.toInt(v.length > 1 ? v[1] : 0), NumberConversions.toInt(v.length > 2 ? v[2] : 0)).origin(in);
        } else if (in.startsWith("month:")) {
            String[] v = in.substring("month:".length()).split(":");
            return new Time(TimeType.MONTH, NumberConversions.toInt(v[0]), NumberConversions.toInt(v.length > 1 ? v[1] : 0), NumberConversions.toInt(v.length > 1 ? v[2] : 0)).origin(in);
        } else {
            return new Time(in).origin(in);
        }
    }

    public static Time parseNoTime(String in) {
        if (in == null) {
            return null;
        }
        in = in.toLowerCase();
        if (in.startsWith("week:")) {
            String[] v = in.substring("week:".length()).split(":");
            return new Time(TimeType.WEEK, NumberConversions.toInt(v[0]), NumberConversions.toInt(v.length > 1 ? v[1] : 0), NumberConversions.toInt(v.length > 2 ? v[2] : 0)).origin(in);
        } else if (in.startsWith("month:")) {
            String[] v = in.substring("month:".length()).split(":");
            return new Time(TimeType.MONTH, NumberConversions.toInt(v[0]), NumberConversions.toInt(v.length > 1 ? v[1] : 0), NumberConversions.toInt(v.length > 1 ? v[2] : 0)).origin(in);
        } else {
            String[] v = in.split(":");
            return new Time(NumberConversions.toInt(v[0]), NumberConversions.toInt(v.length > 1 ? v[1] : 0)).origin(in);
        }
    }

    public TimeType getType() {
        return type;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public long getTime() {
        return time;
    }

    public String getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "Time{" +
                "type=" + type +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", time=" + time +
                ", cacheEnd=" + cacheEnd +
                ", end=" + end +
                ", origin='" + origin + '\'' +
                '}';
    }
}