package taboolib.module.effect.shape;

import taboolib.common.util.Location;
import taboolib.common.util.Vector;
import taboolib.module.effect.ParticleObj;
import taboolib.module.effect.ParticleSpawner;
import taboolib.module.effect.utils.VectorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示一个N角星, N必须是一个奇数
 *
 * @author Zoyn
 */
public class NStar extends ParticleObj {

    private double angle;
    private int corner;
    private double radius;
    private double step;

    public NStar(Location origin, int corner, double radius, double step, ParticleSpawner spawner) {
        super(spawner);
        if (corner % 2 == 0) {
            throw new IllegalArgumentException("N角星的 corner 参数必须为一个奇数整数!");
        }
        setOrigin(origin);
        this.corner = corner;
        this.radius = radius;
        this.step = step;

        this.angle = 360D / corner;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();
        double x = radius * Math.cos(Math.toRadians(angle));
        double z = radius * Math.sin(Math.toRadians(angle));

        double x2 = radius * Math.cos(Math.toRadians(angle * 3));
        double z2 = radius * Math.sin(Math.toRadians(angle * 3));

        Vector START = new Vector(x2 - x, 0, z2 - z);
        double length = START.length();
        START.normalize();
        Location end = getOrigin().clone().add(x, 0, z);

        for (int i = 1; i <= corner; i++) {
            for (double j = 0; j < length; j += step) {
                Vector vectorTemp = START.clone().multiply(j);
                Location spawnLocation = end.clone().add(vectorTemp);

                points.add(spawnLocation);
            }
            Vector vectorTemp = START.clone().multiply(length);
            end = end.clone().add(vectorTemp);

            VectorUtils.rotateAroundAxisY(START, 180 - angle / 2);
        }
        // 做一个对 Matrix 和 Increment 的兼容
        return points.stream().map(location -> {
            Location showLocation = location;
            if (hasMatrix()) {
                Vector v = new Vector(location.getX() - getOrigin().getX(), location.getY() - getOrigin().getY(), location.getZ() - getOrigin().getZ());
                Vector changed = getMatrix().applyVector(v);

                showLocation = getOrigin().clone().add(changed);
            }

            showLocation.add(getIncrementX(), getIncrementY(), getIncrementZ());
            return showLocation;
        }).collect(Collectors.toList());
    }

    @Override
    public void show() {
        double x = radius * Math.cos(Math.toRadians(angle));
        double z = radius * Math.sin(Math.toRadians(angle));

        double x2 = radius * Math.cos(Math.toRadians(angle * 3));
        double z2 = radius * Math.sin(Math.toRadians(angle * 3));

        Vector START = new Vector(x2 - x, 0, z2 - z);
        double length = START.length();
        START.normalize();
        Location end = getOrigin().clone().add(x, 0, z);

        for (int i = 1; i <= corner; i++) {
            for (double j = 0; j < length; j += step) {
                Vector vectorTemp = START.clone().multiply(j);
                Location spawnLocation = end.clone().add(vectorTemp);

                spawnParticle(spawnLocation);
            }
            Vector vectorTemp = START.clone().multiply(length);
            end = end.clone().add(vectorTemp);

            VectorUtils.rotateAroundAxisY(START, 180 - angle / 2);
        }
    }

    public double getAngle() {
        return angle;
    }

    public NStar setAngle(double angle) {
        this.angle = angle;
        return this;
    }

    public int getCorner() {
        return corner;
    }

    public NStar setCorner(int corner) {
        this.corner = corner;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public NStar setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public double getStep() {
        return step;
    }

    public NStar setStep(double step) {
        this.step = step;
        return this;
    }
}
