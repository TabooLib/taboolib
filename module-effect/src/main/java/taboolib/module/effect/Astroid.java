package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

/**
 * 表示一个星形线
 *
 * @author Zoyn
 */
@Isolated
public class Astroid extends ParticleObj {

    private double radius;

    /**
     * 构造一个星形线
     *
     * @param origin 原点
     */
    public Astroid(Location origin, ParticleSpawner spawner) {
        this(1D, origin, spawner);
    }

    /**
     * 构造一个星形线
     *
     * @param radius 半径
     * @param origin 原点
     */
    public Astroid(double radius, Location origin, ParticleSpawner spawner) {
        super(spawner);
        this.radius = radius;
        setOrigin(origin);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public void show() {
        for (double t = 0.0D; t < 360.0D; t++) {
            double radians = Math.toRadians(t);
            // 计算公式
            double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
            double z = Math.pow(this.radius * Math.sin(radians), 3.0D);
            spawnParticle(getOrigin().clone().add(x, 0, z));
        }
    }
}
