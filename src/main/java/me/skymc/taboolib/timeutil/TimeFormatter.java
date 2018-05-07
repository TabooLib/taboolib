package me.skymc.taboolib.timeutil;

import java.util.concurrent.TimeUnit;

/**
 * @author sky
 * @since 2018-04-10 22:11:04
 */
public class TimeFormatter {

    private long days;
    private long hours;
    private long minutes;
    private long seconds;
    private long milliseconds;

    public long getDays() {
        return days;
    }

    public void setDays(long days) {
        this.days = days;
    }

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public TimeFormatter(long millisecond) {
        days = TimeUnit.MILLISECONDS.toDays(millisecond);
        hours = TimeUnit.MILLISECONDS.toHours(millisecond) - days * 24L;
        minutes = TimeUnit.MILLISECONDS.toMinutes(millisecond) - TimeUnit.MILLISECONDS.toHours(millisecond) * 60L;
        seconds = TimeUnit.MILLISECONDS.toSeconds(millisecond) - TimeUnit.MILLISECONDS.toMinutes(millisecond) * 60L;
        milliseconds = TimeUnit.MILLISECONDS.toMillis(millisecond) - TimeUnit.MILLISECONDS.toSeconds(millisecond) * 1000L;
    }

    public long toMilliseconds() {
        return milliseconds + (seconds * 1000L) + (minutes * 1000L * 60L) + (hours * 1000L * 60L * 60L) + (days * 1000L * 60L * 60L * 24L);
    }
}
