package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * 构造一个球
 * <p>算法来源: https://stackoverflow.com/questions/9600801/evenly-distributing-n-points-on-a-sphere/26127012#26127012</p>
 *
 * @author Zoyn
 */
@Isolated
public class Sphere extends ParticleObj {

    /**
     * 黄金角度 约等于137.5度
     */
    private final double phi = Math.PI * (3D - Math.sqrt(5));
    private final List<Location> locations;
    private int sample;
    private double radius;

    public Sphere(Location origin, ParticleSpawner spawner) {
        this(origin, 50, 1, spawner);
    }

    /**
     * 构造一个球
     *
     * @param origin 球的圆点
     * @param sample 样本点个数(粒子的数量)
     * @param radius 球的半径
     */
    public Sphere(Location origin, int sample, double radius, ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        this.sample = sample;
        this.radius = radius;
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

    public int getSample() {
        return sample;
    }

    public Sphere setSample(int sample) {
        this.sample = sample;
        resetLocations();
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public Sphere setRadius(double radius) {
        this.radius = radius;
        resetLocations();
        return this;
    }

    public void resetLocations() {
        locations.clear();
        for (int i = 0; i < 1000; i++) {
            // y goes from 1 to -1
            double y = 1 - (i / (sample - 1f)) * 2;
            // radius at y
            double yRadius = Math.sqrt(1 - y * y);
            // golden angle increment
            double theta = phi * i;
            double x = Math.cos(theta) * radius * yRadius;
            double z = Math.sin(theta) * radius * yRadius;
            y *= radius;
            locations.add(getOrigin().clone().add(x, y, z));
        }
    }
}
