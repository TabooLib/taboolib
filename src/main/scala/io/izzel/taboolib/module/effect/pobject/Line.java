package io.izzel.taboolib.module.effect.pobject;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * 表示一个线
 *
 * @author Zoyn
 */
public class Line extends ParticleObject {

    private Vector vector;
    private Location start;
    private Location end;
    /**
     * 步长
     */
    private double step;
    /**
     * 向量长度
     */
    private double length;

    public Line(Location start, Location end) {
        this(start, end, 0.1);
    }

    /**
     * 构造一个线
     *
     * @param start 线的起点
     * @param end 线的终点
     * @param step 每个粒子之间的间隔 (也即步长)
     */
    public Line(Location start, Location end, double step) {
        this(start, end, step, 20L);
    }

    /**
     * 构造一个线
     *
     * @param start   线的起点
     * @param end   线的终点
     * @param step   每个粒子之间的间隔 (也即步长)
     * @param period 特效周期(如果需要可以使用)
     */
    public Line(Location start, Location end, double step, long period) {
        this.start = start;
        this.end = end;
        this.step = step;
        setPeriod(period);

        // 对向量进行重置
        resetVector();
    }

    @Override
    public void show() {
        for (double i = 0; i < length; i += step) {
            Vector vectorTemp = vector.clone().multiply(i);
            start.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, start.clone().add(vectorTemp), 1);
        }
    }

//    @Override
//    public void alwaysShow() {
//        turnOffTask();
//
//        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
//        Bukkit.getScheduler().runTaskLater(ParticleLib.getInstance(), () -> {
//            running = true;
//            task = new BukkitRunnable() {
//                @Override
//                public void run() {
//                    if (!running) {
//                        return;
//                    }
//                    show();
//                }
//            }.runTaskTimer(ParticleLib.getInstance(), 0L, period);
//
//            setShowType(ShowType.ALWAYS_SHOW);
//        }, 2L);
//    }
//
//    @Override
//    public void alwaysShowAsync() {
//        turnOffTask();
//
//        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
//        Bukkit.getScheduler().runTaskLater(ParticleLib.getInstance(), () -> {
//            running = true;
//            task = new BukkitRunnable() {
//                @Override
//                public void run() {
//                    if (!running) {
//                        return;
//                    }
//                    show();
//                }
//            }.runTaskTimerAsynchronously(ParticleLib.getInstance(), 0L, period);
//
//            setShowType(ShowType.ALWAYS_SHOW_ASYNC);
//        }, 2L);
//    }

    public Location getStart() {
        return start;
    }

    public Line setStart(Location start) {
        this.start = start;
        resetVector();
        return this;
    }

    public Location getEnd() {
        return end;
    }

    public Line setEnd(Location end) {
        this.end = end;
        resetVector();
        return this;
    }

    public double getStep() {
        return step;
    }

    public Line setStep(double step) {
        this.step = step;
        resetVector();
        return this;
    }

    public void resetVector() {
        vector = end.clone().subtract(start).toVector();
        length = vector.length();
        vector.normalize();
    }
}
