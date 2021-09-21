package taboolib.module.effect;

import kotlin.Unit;
import taboolib.common.Isolated;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.common.util.Location;

/**
 * 表示一个弧
 *
 * @author Zoyn
 */
@Isolated
public class Arc extends ParticleObj implements Playable {

    private double startAngle = 0D;
    private double angle;
    private double radius;
    private double step;
    private double currentAngle = 0D;

    public Arc(Location origin, ParticleSpawner spawner) {
        this(origin, 30D, spawner);
    }

    public Arc(Location origin, double angle, ParticleSpawner spawner) {
        this(origin, angle, 1D, spawner);
    }

    public Arc(Location origin, double angle, double radius, ParticleSpawner spawner) {
        this(origin, angle, radius, 1, spawner);
    }

    /**
     * 构造一个弧
     *
     * @param origin 弧所在的圆的圆点
     * @param angle  弧所占的角度
     * @param radius 弧所在的圆的半径
     * @param step   每个粒子的间隔(也即步长)
     */
    public Arc(Location origin, double angle, double radius, double step, ParticleSpawner spawner) {
        this(origin, angle, radius, step, 20L, spawner);
    }

    /**
     * 从零度角开始构造一个弧
     *
     * @param origin 弧所在的圆的圆点
     * @param angle  弧所占的角度
     * @param radius 弧所在的圆的半径
     * @param step   每个粒子的间隔(也即步长)
     * @param period 特效周期(如果需要可以使用)
     */
    public Arc(Location origin, double angle, double radius, double step, long period, ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        this.angle = angle;
        this.radius = radius;
        this.step = step;
        setPeriod(period);
    }

    /**
     * 从给定的开始角构造一个弧
     *
     * @param origin     弧所在的圆的圆点
     * @param startAngle 开始角
     * @param angle      弧总共的角
     * @param radius     弧所占半径
     * @param step       每个粒子的间隔
     * @param period     特效周期(如果需要可以使用)
     * @param spawner    粒子生成器
     */
    public Arc(Location origin, double startAngle, double angle, double radius, double step, long period, ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        this.startAngle = startAngle;
        this.angle = angle;
        this.radius = radius;
        this.step = step;
        setPeriod(period);
    }

    @Override
    public void show() {
        for (double i = startAngle; i < angle; i += step) {
            double radians = Math.toRadians(i);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);
            spawnParticle(getOrigin().clone().add(x, 0, z));
        }
    }

    @Override
    public void play() {
        currentAngle = startAngle;

        ExecutorKt.submit(false, false, 0, getPeriod(), null, task -> {
            // 进行关闭
            if (currentAngle > angle) {
                task.cancel();
                return Unit.INSTANCE;
            }
            currentAngle += step;
            double radians = Math.toRadians(currentAngle);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            spawnParticle(getOrigin().clone().add(x, 0, z));
            return Unit.INSTANCE;
        });
    }

    @Override
    public void playNextPoint() {
        currentAngle += step;
        double radians = Math.toRadians(currentAngle);
        double x = radius * Math.cos(radians);
        double z = radius * Math.sin(radians);

        spawnParticle(getOrigin().clone().add(x, 0, z));

        // 进行重置
        if (currentAngle > angle) {
            currentAngle = startAngle;
        }
    }

    public double getStartAngle() {
        return startAngle;
    }

    public Arc setStartAngle(double startAngle) {
        this.startAngle = startAngle;
        return this;
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
