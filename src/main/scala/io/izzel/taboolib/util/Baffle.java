package io.izzel.taboolib.util;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 冷却工具
 *
 * @author sky
 * @since 2020-10-02 04:35
 */
public abstract class Baffle {

    /**
     * 验证个体（*）的执行结果
     *
     * @return 是否运行
     */
    public boolean hasNext() {
        return hasNext("*");
    }

    /**
     * 验证个体的执行结果
     *
     * @param id 个体序号
     * @return 是否运行
     */
    abstract public boolean hasNext(String id);

    /**
     * 重置个体（*）的执行缓存
     */
    public void reset() {
        reset("*");
    }

    /**
     * 重置个体（*）的执行缓存
     *
     * @param id 个体序号
     */
    abstract public void reset(String id);

    /**
     * 重置所有数据
     */
    abstract public void resetAll();

    /**
     * 按时间阻断（类似 Cooldowns）
     * 单位：毫秒
     *
     * @param duration 时间数值
     * @param timeUnit 时间单位
     */
    @NotNull
    public static Baffle of(long duration, TimeUnit timeUnit) {
        return new BaffleTime(timeUnit.toMillis(duration));
    }

    /**
     * 按次阻断（类似 SimpleCounter）
     *
     * @param count 次数
     */
    @NotNull
    public static Baffle of(int count) {
        return new BaffleCounter(count);
    }

    public static class BaffleTime extends Baffle {

        private final long millis;
        private final Map<String, Long> data = Maps.newConcurrentMap();

        public BaffleTime(long millis) {
            this.millis = millis;
        }

        public long nextTime(String id) {
            return hasNext(id) ? (data.get(id) + millis) - System.currentTimeMillis() : 0L;
        }

        @Override
        public boolean hasNext(String id) {
            long time = data.getOrDefault(id, 0L);
            if (time + millis > System.currentTimeMillis()) {
                data.put(id, System.currentTimeMillis());
                return true;
            }
            return false;
        }

        @Override
        public void reset(String id) {
            data.remove(id);
        }

        @Override
        public void resetAll() {
            data.clear();
        }
    }

    public static class BaffleCounter extends Baffle {

        private final int count;
        private final Map<String, Integer> data = Maps.newConcurrentMap();

        public BaffleCounter(int count) {
            this.count = count;
        }

        @Override
        public boolean hasNext(String id) {
            int i = data.computeIfAbsent(id, a -> 0);
            if (i < count) {
                data.put(id, i + 1);
                return false;
            }
            data.put(id, 0);
            return true;
        }

        @Override
        public void reset(String id) {
            data.remove(id);
        }

        @Override
        public void resetAll() {
            data.clear();
        }
    }
}
