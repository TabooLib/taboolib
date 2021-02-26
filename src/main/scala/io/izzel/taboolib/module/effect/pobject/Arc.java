package io.izzel.taboolib.module.effect.pobject;

import org.bukkit.Location;

/**
 * 表示一个弧
 *
 * @author Zoyn
 */
public class Arc extends ParticleObject {

    private double angle;
    private double radius;
    private double step;

    public Arc(Location origin) {
        this(origin, 30D);
    }

    public Arc(Location origin, double angle) {
        this(origin, angle, 1D);
    }

    public Arc(Location origin, double angle, double radius) {
        this(origin, angle, radius, 1);
    }

    /**
     * 构造一个弧
     *
     * @param origin 弧所在的圆的圆点
     * @param angle  弧所占的角度
     * @param radius 弧所在的圆的半径
     * @param step   每个粒子的间隔(也即步长)
     */
    public Arc(Location origin, double angle, double radius, double step) {
        this(origin, angle, radius, step, 20L);
    }

    /**
     * 构造一个弧
     *
     * @param origin 弧所在的圆的圆点
     * @param angle  弧所占的角度
     * @param radius 弧所在的圆的半径
     * @param step   每个粒子的间隔(也即步长)
     * @param period 特效周期(如果需要可以使用)
     */
    public Arc(Location origin, double angle, double radius, double step, long period) {
        setOrigin(origin);
        this.angle = angle;
        this.radius = radius;
        this.step = step;
        setPeriod(period);
    }

    @Override
    public void show() {
        for (int i = 0; i < angle; i += step) {
            double radians = Math.toRadians(i);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            spawnParticle(getOrigin().clone().add(x, 0, z));
//            origin.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, origin.clone().add(x, 0, z), 1);
        }
    }

    public double getAngle() {
        return angle;
    }

    public Arc setAngle(double angle) {
        this.angle = angle;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public Arc setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public double getStep() {
        return step;
    }

    public Arc setStep(double step) {
        this.step = step;
        return this;
    }

}
