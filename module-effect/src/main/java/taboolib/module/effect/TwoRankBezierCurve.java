package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;
import taboolib.common.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一条二阶贝塞尔曲线
 * <p>给定三点, 自动生成一条二阶贝塞尔曲线</p>
 *
 * @author Zoyn
 */
@Isolated
public class TwoRankBezierCurve extends ParticleObj {

    private final List<Location> locations;
    private Location p0;
    private Location p1;
    private Location p2;
    private double step;

    public TwoRankBezierCurve(Location p0, Location p1, Location p2, ParticleSpawner spawner) {
        this(p0, p1, p2, 0.05, spawner);
    }

    /**
     * 构造一个三阶贝塞尔曲线
     *
     * @param p0   连续点
     * @param p1   控制点
     * @param p2   控制点
     * @param step 每个粒子的间隔(也即步长)
     */
    public TwoRankBezierCurve(Location p0, Location p1, Location p2, double step, ParticleSpawner spawner) {
        super(spawner);
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.step = step;
        locations = new ArrayList<>();
        resetLocations();
    }

    @Override
    public void show() {
        locations.forEach(loc -> {
            if (loc != null) {
                spawnParticle(loc);
            }
        });
    }

    public Location getP0() {
        return p0;
    }

    public TwoRankBezierCurve setP0(Location p0) {
        this.p0 = p0;
        resetLocations();
        return this;
    }

    public Location getP1() {
        return p1;
    }

    public TwoRankBezierCurve setP1(Location p1) {
        this.p1 = p1;
        resetLocations();
        return this;
    }

    public Location getP2() {
        return p2;
    }

    public TwoRankBezierCurve setP2(Location p2) {
        this.p2 = p2;
        resetLocations();
        return this;
    }

    public double getStep() {
        return step;
    }

    public TwoRankBezierCurve setStep(double step) {
        this.step = step;
        resetLocations();
        return this;
    }

    /**
     * 重新计算贝塞尔曲线上的点
     */
    public void resetLocations() {
        locations.clear();
        // 算法
        // 算了我知道很蠢这个算法...
        for (double t = 0; t < 1; t += step) {
            Vector v1 = p1.clone().subtract(p0).toVector();
            Location t1 = p0.clone().add(v1.multiply(t));
            Vector v2 = p2.clone().subtract(p1).toVector();
            Location t2 = p1.clone().add(v2.multiply(t));
            Vector v3 = t2.clone().subtract(t1).toVector();
            Location destination = t1.clone().add(v3.multiply(t));
            locations.add(destination.clone());
        }
    }
}
