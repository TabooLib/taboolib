package taboolib.common5;

import com.google.common.collect.Maps;
import taboolib.common.io.Isolated;
import taboolib.common5.util.String2TimeKt;

import java.util.Calendar;
import java.util.Map;

/**
 * @author 坏黑
 * @since 2019-05-28 17:28
 */
@Isolated
public class TimeCycle {

    private static final Map<String, TimeCycle> cacheMap = Maps.newHashMap();
    private final TimeCycleUnit type;
    private int day;
    private int hour;
    private int minute;
    private long time;
    private final Map<Long, Calendar> cacheEnd = Maps.newHashMap();
    private Calendar end;
    private String origin;

    public TimeCycle(String millis) {
        this(String2TimeKt.parseMillis(millis));
    }

    public TimeCycle(long time) {
        this.type = TimeCycleUnit.TIME;
        this.time = time;
    }

    public TimeCycle(int hour, int minute) {
        this.type = TimeCycleUnit.DAY;
        this.hour = hour;
        this.minute = minute;
    }

    public TimeCycle(TimeCycleUnit type, int day, int hour, int minute) {
        this.type = type;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public TimeCycle origin(String origin) {
        this.origin = origin;
        return this;
    }

    public TimeCycle in(long start) {
        if (this.cacheEnd.containsKey(start)) {
            this.end = this.cacheEnd.get(start);
        } else {
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(start);
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(start);
            this.end = endCal;
            this.end.set(Calendar.SECOND, 0);
            this.end.set(Calendar.MILLISECOND, 0);
            this.cacheEnd.put(start, this.end);
            if (this.type != TimeCycleUnit.TIME) {
                switch (this.type) {
                    case DAY:
                        this.end.set(Calendar.HOUR_OF_DAY, hour);
                        this.end.set(Calendar.MINUTE, minute);
                        if (startCal.after(this.end)) {
                            this.end.add(Calendar.DATE, 1);
                        }
                        break;
                    case WEEK:
                        this.end.set(Calendar.DAY_OF_WEEK, day + 1);
                        this.end.set(Calendar.HOUR_OF_DAY, hour);
                        this.end.set(Calendar.MINUTE, minute);
                        if (startCal.after(this.end)) {
                            this.end.add(Calendar.DATE, 7);
                        }
                        break;
                    case MONTH:
                        this.end.set(Calendar.DAY_OF_MONTH, day);
                        this.end.set(Calendar.HOUR_OF_DAY, hour);
                        this.end.set(Calendar.MINUTE, minute);
                        if (startCal.after(this.end)) {
                            this.end.add(Calendar.MONTH, 1);
                        }
                        break;
                }
            }
        }
        return this;
    }

    public boolean isTimeout(long start) {
        return type == TimeCycleUnit.TIME ? start + time < System.currentTimeMillis() : isTimeout();
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

    public TimeCycleUnit getType() {
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

    public Calendar getEnd() {
        return end;
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