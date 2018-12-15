package me.skymc.taboolib.common.schedule;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * @Author 坏黑
 * @Since 2018-12-15 15:25
 */
public class TScheduleData {

    private final TSchedule annotation;
    private final BukkitRunnable runnable;

    public TScheduleData(TSchedule annotation, BukkitRunnable runnable) {
        this.annotation = annotation;
        this.runnable = runnable;
    }

    public TSchedule getAnnotation() {
        return annotation;
    }

    public BukkitRunnable getRunnable() {
        return runnable;
    }
}
