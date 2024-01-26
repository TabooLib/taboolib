package taboolib.common5;

import java.util.Calendar;
import java.util.function.Function;

/**
 * @author sky
 * @since 2020-02-18 14:13
 */
public enum RealTime {

    /**
     * 周日开始，周六结束
     */
    START_IN_SUNDAY(r -> {
        Calendar time = Calendar.getInstance();
        switch (r.unit) {
            case HOUR: {
                // 重置日期
                time.set(Calendar.MILLISECOND, 0);
                time.set(Calendar.SECOND, 0);
                time.set(Calendar.MINUTE, 0);
                // 推进日期
                time.add(Calendar.HOUR, r.value);
                return time.getTimeInMillis();
            }
            case DAY: {
                // 重置日期
                time.set(Calendar.MILLISECOND, 0);
                time.set(Calendar.SECOND, 0);
                time.set(Calendar.MINUTE, 0);
                time.set(Calendar.HOUR_OF_DAY, 0);
                // 推进日期
                time.add(Calendar.DAY_OF_YEAR, r.value);
                return time.getTimeInMillis();
            }
            case WEEK: {
                // 重置日期
                time.set(Calendar.MILLISECOND, 0);
                time.set(Calendar.SECOND, 0);
                time.set(Calendar.MINUTE, 0);
                time.set(Calendar.HOUR_OF_DAY, 0);
                time.set(Calendar.DAY_OF_WEEK, 1);
                // 推进日期
                time.add(Calendar.WEEK_OF_YEAR, r.value);
                return time.getTimeInMillis();
            }
        }
        return 0L;
    }),

    /**
     * 周一开始，周日结束
     */
    START_IN_MONDAY(r -> {
        Calendar time = Calendar.getInstance();
        switch (r.unit) {
            case DAY:
            case HOUR: {
                return START_IN_SUNDAY.next.apply(r);
            }
            case WEEK: {
                // 判断周日
                if (time.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    // 推进日
                    time.add(Calendar.DAY_OF_YEAR, 1 + ((r.value - 1) * 7));
                } else {
                    // 推进周
                    time.add(Calendar.WEEK_OF_YEAR, r.value);
                    // 重置日
                    time.set(Calendar.DAY_OF_WEEK, 2);
                }
                // 重置时
                time.set(Calendar.HOUR_OF_DAY, 0);
                // 重置分
                time.set(Calendar.MINUTE, 0);
                // 重置秒
                time.set(Calendar.SECOND, 0);
                return time.getTimeInMillis();
            }
        }
        return 0L;
    });

    final Function<NextTime, Long> next;

    RealTime(Function<NextTime, Long> next) {
        this.next = next;
    }

    /**
     * 获取下一周期的起始时间
     */
    public long nextTime(Type unit, int value) {
        return next.apply(new NextTime(unit, value));
    }

    static class NextTime {

        private final Type unit;
        private final int value;

        public NextTime(Type unit, int value) {
            this.unit = unit;
            this.value = value;
        }
    }

    public enum Type {

        HOUR, DAY, WEEK
    }
}

