package io.izzel.taboolib.effect.pobject;

import io.izzel.taboolib.TabooLib;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * 特效对象
 *
 * @author Zoyn
 */
public abstract class ParticleObject {

    private ShowType showType = ShowType.NONE;
    private BukkitTask task;
    private long period;
    private boolean running = false;

    public abstract void show();

    public void alwaysShow() {
        turnOffTask();

        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        Bukkit.getScheduler().runTaskLater(TabooLib.getPlugin(), () -> {
            running = true;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!running) {
                        return;
                    }
                    show();
                }
            }.runTaskTimer(TabooLib.getPlugin(), 0L, period);

            setShowType(ShowType.ALWAYS_SHOW);
        }, 2L);
    }

    public void alwaysShowAsync() {
        turnOffTask();

        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        Bukkit.getScheduler().runTaskLater(TabooLib.getPlugin(), () -> {
            running = true;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!running) {
                        return;
                    }
                    show();
                }
            }.runTaskTimerAsynchronously(TabooLib.getPlugin(), 0L, period);

            setShowType(ShowType.ALWAYS_SHOW_ASYNC);
        }, 2L);
    }

    public void turnOffTask() {
        if (task != null) {
            running = false;
            task.cancel();
            setShowType(ShowType.NONE);
        }
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public ShowType getShowType() {
        return showType;
    }

    public void setShowType(ShowType showType) {
        this.showType = showType;
    }

}
