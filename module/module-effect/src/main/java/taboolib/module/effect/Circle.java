package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

/**
 * 表示一个圆
 *
 * @author Zoyn
 */
@Isolated
public class Circle extends Arc {

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
        super(origin, 0D, 360D, radius, step, period, spawner);
    }

}
