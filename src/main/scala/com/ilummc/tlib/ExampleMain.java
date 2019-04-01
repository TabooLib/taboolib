package com.ilummc.tlib;

import com.ilummc.tlib.annotations.TConfig;
import com.ilummc.tlib.bean.Property;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.TimeUnit;

@TConfig(name = "cfg.yml", charset = "GBK")
public class ExampleMain extends JavaPlugin {

    private Property<Boolean> update = Property.of(false);

    public static void main(String[] args) {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        System.out.println(bean.getHeapMemoryUsage().toString());
        System.out.println(bean.getNonHeapMemoryUsage().toString());
        for (int i = 0; i < 10; i++) {
            for (GarbageCollectorMXBean mxBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                System.out.println(mxBean.getName());
                System.out.println(mxBean.getCollectionCount());
                System.out.println(mxBean.getCollectionTime());
                for (String s : mxBean.getMemoryPoolNames()) {
                    System.out.println(s);
                }
                System.out.println(mxBean.getObjectName().toString());
            }
            System.gc();
        }
        for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            System.out.println(s);
        }
    }

    @Override
    public void onEnable() {
        update.addListener(((oldVal, newVal) -> {
            Bukkit.getLogger().info("配置项 enableUpdate 的值由 " + oldVal + " 变为了 " + newVal);
            if (newVal) {
                Updater.start();
            } else {
                Updater.stop();
            }
        }));
    }

    private static class Updater {
        public static void start() {

        }

        public static void stop() {

        }
    }

    private static class CD {

        final long start, period;
        final TimeUnit unit;
        final Runnable onStart, onFinish, onTimer;

        CD(long start, long period, TimeUnit unit, Runnable onStart, Runnable onFinish, Runnable onTimer) {
            this.start = start;
            this.period = period;
            this.unit = unit;
            this.onStart = onStart;
            this.onFinish = onFinish;
            this.onTimer = onTimer;
        }

        public static void main(String[] args) {
            CD.builder().setOnStart(() -> {
            }).setOnFinish(() -> {
            }).setOnTimer(1000, TimeUnit.MILLISECONDS, () -> {
            }).build();
        }

        public static CdBuilder builder() {
            return new CdBuilder();
        }

        private static class CdBuilder {
            private long start, period;
            private TimeUnit unit;
            private Runnable onStart, onFinish, onTimer;

            public CdBuilder setOnStart(Runnable runnable) {
                this.onStart = runnable;
                return this;
            }

            public CdBuilder setOnFinish(Runnable runnable) {
                this.onFinish = runnable;
                return this;
            }

            public CdBuilder setOnTimer(long period, TimeUnit timeUnit, Runnable runnable) {
                this.period = period;
                this.unit = timeUnit;
                this.onTimer = runnable;
                return this;
            }

            public CD build() {
                return new CD(start, period, unit, onStart, onFinish, onTimer);
            }
        }
    }
}
