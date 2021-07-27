package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;
import taboolib.common.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一条三阶贝塞尔曲线
 * <p>给定四点, 自动生成一条三阶贝塞尔曲线</p>
 *
 * @author Zoyn
 */
@Isolated
public class ThreeRankBezierCurve extends ParticleObj {

    private final List<Location> locations;
    private Location p0;
    private Location p1;
    private Location p2;
    private Location p3;
    private double step;

    public ThreeRankBezierCurve(Location p0, Location p1, Location p2, Location p3, ParticleSpawner spawner) {
        this(p0, p1, p2, p3, 0.05, spawner);
    }

    /**
     * 构造一个三阶贝塞尔曲线
     *
     * @param p0   连续点
     * @param p1   控制点
     * @param p2   控制点
     * @param p3   连续点
     * @param step 每个粒子的间隔(也即步长)
     */
    public ThreeRankBezierCurve(Location p0, Location p1, Location p2, Location p3, double step, ParticleSpawner spawner) {
        super(spawner);
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
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

    public ThreeRankBezierCurve setP0(Location p0) {
        this.p0 = p0;
        resetLocations();
        return this;
    }

    public Location getP1() {
        return p1;
    }

    public ThreeRankBezierCurve setP1(Location p1) {
        this.p1 = p1;
        resetLocations();
        return this;
    }

    public Location getP2() {
        return p2;
    }

    public ThreeRankBezierCurve setP2(Location p2) {
        this.p2 = p2;
        resetLocations();
        return this;
    }

    public Location getP3() {
        return p3;
    }

    public ThreeRankBezierCurve setP3(Location p3) {
        this.p3 = p3;
        resetLocations();
        return this;
    }

    public double getStep() {
        return step;
    }

    public ThreeRankBezierCurve setStep(double step) {
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
        for (double t = 0; t < 1; t += 0.05) {
            Vector v1 = p1.clone().subtract(p0).toVector();
            Location t1 = p0.clone().add(v1.multiply(t));
            Vector v2 = p2.clone().subtract(p1).toVector();
            Location t2 = p1.clone().add(v2.multiply(t));
            Vector v3 = p3.clone().subtract(p2).toVector();
            Location t3 = p2.clone().add(v3.multiply(t));
            Vector dv1 = t2.clone().subtract(t1).toVector();
            Location d1 = t1.clone().add(dv1.multiply(t));
            Vector dv2 = t3.clone().subtract(t2).toVector();
            Location d2 = t2.clone().add(dv2.multiply(t));
            Vector f1 = d2.clone().subtract(d1).toVector();
            Location destination = d1.clone().add(f1.multiply(t));
            locations.add(destination.clone());
        }
    }
}
