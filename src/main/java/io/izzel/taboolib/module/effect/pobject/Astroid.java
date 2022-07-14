package io.izzel.taboolib.module.effect.pobject;

import org.bukkit.Location;

/**
 * 表示一个星形线
 *
 * @author Zoyn
 */
public class Astroid extends ParticleObject {

    private double radius;

    /**
     * 构造一个星形线
     *
     * @param origin 原点
     */
    public Astroid(Location origin) {
        this(1D, origin);
    }

    /**
     * 构造一个星形线
     *
     * @param radius 半径
     * @param origin 原点
     */
    public Astroid(double radius, Location origin) {
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
