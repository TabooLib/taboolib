package io.izzel.taboolib.module.effect.pobject;

import org.bukkit.Location;

public class Circle extends ParticleObject {

    private final Arc fullArc;

    public Circle(Location origin) {
        this(origin, 1);
    }

    public Circle(Location origin, double radius) {
        this(origin, radius, 1);
    }

    /**
     * 构造一个圆
     *
     * @param origin 圆的圆点
     * @param radius 圆的半径
     * @param step   每个粒子的间隔(也即步长)
     */
    public Circle(Location origin, double radius, double step) {
        this(origin, radius, step, 20L);
    }

    /**
     * 构造一个圆
     *
     * @param origin 圆的圆点
     * @param radius 圆的半径
     * @param step   每个粒子的间隔(也即步长)
     * @param period 特效周期(如果需要可以使用)
     */
    public Circle(Location origin, double radius, double step, long period) {
        // Circle只需要控制这个fullArc就可以满足所有的要求
        this.fullArc = new Arc(origin)
                .setAngle(360D)
                .setRadius(radius)
                .setStep(step);
        fullArc.setPeriod(period);
    }

    @Override
    public void show() {
        fullArc.show();
    }

    @Override
    public void alwaysShow() {
        fullArc.alwaysShow();
        // 再设置Circle自身的ShowType
        setShowType(ShowType.ALWAYS_SHOW);
    }

    @Override
    public void alwaysShowAsync() {
        fullArc.alwaysShowAsync();
        // 再设置Circle自身的ShowType
        setShowType(ShowType.ALWAYS_SHOW_ASYNC);
    }

    @Override
    public void turnOffTask() {
        fullArc.turnOffTask();
        // 再设置Circle自身的ShowType
        setShowType(ShowType.NONE);
    }

    public Location getOrigin() {
        return fullArc.getOrigin();
    }

    public Circle setOrigin(Location origin) {
        this.fullArc.setOrigin(origin);
        // 之前脑子坏掉了忘记给fullArc设置origin了, 现在不用了
//        checkArcThenRefreshShow();
        return this;
    }

    public double getRadius() {
        return this.fullArc.getRadius();
    }

    public Circle setRadius(double radius) {
        this.fullArc.setRadius(radius);
        return this;
    }

    public double getStep() {
        return this.fullArc.getStep();
    }

    public Circle setStep(double step) {
        this.fullArc.setStep(step);
        return this;
    }

    public long getPeriod() {
        return this.fullArc.getPeriod();
    }

    public void setPeriod(long period) {
        this.fullArc.setPeriod(period);
    }

}
