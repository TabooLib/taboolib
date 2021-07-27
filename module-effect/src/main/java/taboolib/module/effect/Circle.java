package taboolib.module.effect;

import org.jetbrains.annotations.NotNull;
import taboolib.common.Isolated;
import taboolib.common.util.Location;

/**
 * 表示一个圆
 *
 * @author Zoyn
 */
@Isolated
public class Circle extends ParticleObj {

    private final Arc fullArc;

    public Circle(Location origin, ParticleSpawner spawner) {
        this(origin, 1, spawner);
    }

    public Circle(Location origin, double radius, ParticleSpawner spawner) {
        this(origin, radius, 1, spawner);
    }

    /**
     * 构造一个圆
     *
     * @param origin 圆的圆点
     * @param radius 圆的半径
     * @param step   每个粒子的间隔(也即步长)
     */
    public Circle(Location origin, double radius, double step, ParticleSpawner spawner) {
        this(origin, radius, step, 20L, spawner);
    }

    /**
     * 构造一个圆
     *
     * @param origin 圆的圆点
     * @param radius 圆的半径
     * @param step   每个粒子的间隔(也即步长)
     * @param period 特效周期(如果需要可以使用)
     */
    public Circle(Location origin, double radius, double step, long period, ParticleSpawner spawner) {
        super(spawner);
        // Circle只需要控制这个fullArc就可以满足所有的要求
        fullArc = new Arc(origin, spawner).setAngle(360D).setRadius(radius).setStep(step);
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

    @NotNull
    public Location getOrigin() {
        return fullArc.getOrigin();
    }

    public void setOrigin(@NotNull Location origin) {
        this.fullArc.setOrigin(origin);
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
