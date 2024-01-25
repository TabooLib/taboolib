package taboolib.common5;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 冷却工具
 *
 * @author sky
 * @since 2020-10-02 04:35
 */
public abstract class Baffle {

    /**
     * 重置所有数据
     */
    abstract public void resetAll();

    /**
     * 重置个体的执行缓存
     *
     * @param id 个体序号
     */
    abstract public void reset(String id);

    /**
     * 强制个体更新数据
     *
     * @param id 个体序号
     */
    abstract public void next(String id);

    /**
     * 验证个体的执行结果
     *
     * @param id     个体序号
     * @param update 是否更新数据
     * @return 是否运行
     */
    abstract public boolean hasNext(String id, boolean update);

    /**
     * 同 {@link Baffle#next(String)}，个体序号为（*）
     */
    public void reset() {
        reset("*");
    }

    /**
     * 同 {@link Baffle#next(String)}，个体序号为（*）
     */
    public void next() {
        next("*");
    }

    /**
     * 同 {@link Baffle#hasNext(String, boolean)}，个体序号为（*）
     *
     * @return boolean
     */
    public boolean hasNext() {
        return hasNext("*");
    }

    /**
     * 同 {@link Baffle#hasNext(String, boolean)}
     *
     * @param id 序号
     * @return boolean
     */
    public boolean hasNext(String id) {
        return hasNext(id, true);
    }

    /**
     * 按时间阻断
     * 单位：毫秒
     *
     * @param duration 时间数值
     * @param timeUnit 时间单位
     * @return {@link Baffle}
     */
    @NotNull
    public static Baffle of(long duration, TimeUnit timeUnit) {
        return new BaffleTime(timeUnit.toMillis(duration));
    }

    /**
     * 按次阻断（类似 SimpleCounter）
     *
     * @param count 次数
     * @return {@link Baffle}
     */
    @NotNull
    public static Baffle of(int count) {
        return new BaffleCounter(count);
    }

    public static class BaffleTime extends Baffle {

        private final long millis;
        private final Map<String, Long> data = new ConcurrentHashMap<>();
        private long globalTime;

        public BaffleTime(long millis) {
            this.millis = millis;
        }

        /**
         * 获取下次执行时间戳，该方法不会刷新数据
         *
         * @param id 个体序号
         * @return long
         */
        public long nextTime(String id) {
            long result;
            if (Objects.equals(id, "*")) {
                result = globalTime + millis - System.currentTimeMillis();
            } else {
                result = data.getOrDefault(id, 0L) + millis - System.currentTimeMillis();
            }
            return result >= 0 ? result : 0L;
        }

        @Override
        public void resetAll() {
            data.clear();
            globalTime = 0L;
        }

        @Override
        public void reset(String id) {
            if (Objects.equals(id, "*")) {
                globalTime = 0L;
            } else {
                data.remove(id);
            }
        }

        @Override
        public void next(String id) {
            if (Objects.equals(id, "*")) {
                globalTime = System.currentTimeMillis();
            } else {
                data.put(id, System.currentTimeMillis());
            }
        }

        @Override
        public boolean hasNext(String id, boolean update) {
            long time;
            if (Objects.equals(id, "*")) {
                time = globalTime;
            } else {
                time = data.getOrDefault(id, 0L);
            }
            if (time + millis < System.currentTimeMillis()) {
                if (update) {
                    next(id);
                }
                return true;
            }
            return false;
        }
    }

    public static class BaffleCounter extends Baffle {

        private final int count;
        private final Map<String, Integer> data = new ConcurrentHashMap<>();
        private final AtomicInteger globalCount = new AtomicInteger();

        public BaffleCounter(int count) {
            this.count = count;
        }

        @Override
        public void resetAll() {
            data.clear();
            globalCount.set(0);
        }

        @Override
        public void reset(String id) {
            if (Objects.equals(id, "*")) {
                globalCount.set(0);
            } else {
                data.remove(id);
            }
        }

        @Override
        public void next(String id) {
            if (Objects.equals(id, "*")) {
                globalCount.getAndIncrement();
            } else {
                data.put(id, data.computeIfAbsent(id, a -> 0) + 1);
            }
        }

        @Override
        public boolean hasNext(String id, boolean update) {
            if (Objects.equals(id, "*")) {
                if (globalCount.get() < count) {
                    if (update) {
                        globalCount.getAndIncrement();
                    }
                    return false;
                }
                if (update) {
                    globalCount.set(0);
                }
            } else {
                int i;
                if (data.containsKey(id)) {
                    i = data.get(id);
                } else {
                    i = 0;
                    data.put(id, 0);
                }
                if (i < count) {
                    if (update) {
                        data.put(id, i + 1);
                    }
                    return false;
                }
                if (update) {
                    data.put(id, 0);
                }
            }
            return true;
        }
    }
}
