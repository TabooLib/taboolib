package taboolib.module.effect.shape;

import taboolib.common.util.Location;
import taboolib.common.util.Vector;
import taboolib.module.effect.ParticleObj;
import taboolib.module.effect.ParticleSpawner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示一个N棱锥特效
 *
 * @author Zoyn
 */
public class Pyramid extends ParticleObj {

    private final List<Location> locations;
    private int side;
    private double height;
    private double step;
    private double radius;
    private Location upLoc;

    public Pyramid(Location origin, int side, ParticleSpawner spawner) {
        this(origin, side, 1, 1, spawner);
    }

    public Pyramid(Location origin, int side, double radius, double height, ParticleSpawner spawner) {
        this(origin, side, radius, height, 0.02, spawner);
    }

    /**
     * 表示一个棱锥特效
     *
     * @param origin 棱锥底面中心点
     * @param side   棱的个数
     * @param radius 底面半径, 中心点到任意一个角的长度
     * @param height 底面中心点到最上方顶点的长度
     * @param step   粒子的间距
     */
    public Pyramid(Location origin, int side, double radius, double height, double step, ParticleSpawner spawner) {
        super(spawner);
        if (side <= 2) {
            throw new IllegalArgumentException("边数不可为小于或等于2的数!");
        }
        this.side = side;
        this.height = height;
        this.step = step;
        this.radius = radius;

        this.locations = new ArrayList<>();
        setOrigin(origin);
    }

    public int getSide() {
        return side;
    }

    public Pyramid setSide(int side) {
        this.side = side;
        resetLocations();
        return this;

    }

    public double getHeight() {
        return height;
    }

    public Pyramid setHeight(double height) {
        this.height = height;
        upLoc = getOrigin().clone().add(0, height, 0);
        resetLocations();
        return this;
    }

    public double getStep() {
        return step;
    }

    public Pyramid setStep(double step) {
        this.step = step;
        resetLocations();
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public Pyramid setRadius(double radius) {
        this.radius = radius;
        resetLocations();
        return this;
    }

    @Override
    public ParticleObj setOrigin(Location origin) {
        super.setOrigin(origin);
        // 重置最上方的 Loc
        upLoc = origin.clone().add(0, height, 0);

        resetLocations();
        return this;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();
        List<Location> temp = new ArrayList<>();

        for (double angle = 0; angle <= 360; angle += 360D / side) {
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians);
            double z = Math.sin(radians);

            temp.add(getOrigin().clone().add(x, 0, z));
        }

        for (int i = 0; i < temp.size(); i++) {
            if (i + 1 == temp.size()) {
                Vector vectorAB = temp.get(i).clone().subtract(temp.get(0)).toVector();
                double vectorLength = vectorAB.length();
                vectorAB.normalize();
                for (double j = 0; j < vectorLength; j += step) {
                    points.add(temp.get(0).clone().add(vectorAB.clone().multiply(j)));
                }
                break;
            }

            Vector vectorAB = temp.get(i + 1).clone().subtract(temp.get(i)).toVector();
            double vectorLength = vectorAB.length();
            vectorAB.normalize();
            for (double j = 0; j < vectorLength; j += step) {
                points.add(temp.get(i).clone().add(vectorAB.clone().multiply(j)));
            }

            // 棱长部分
            vectorAB = temp.get(i).clone().subtract(upLoc).toVector();
            vectorLength = vectorAB.length();
            vectorAB.normalize();
            for (double j = 0; j < vectorLength; j += step) {
                points.add(upLoc.clone().add(vectorAB.clone().multiply(j)));
            }
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
        if (locations.isEmpty()) {
            return;
        }

        for (int i = 0; i < locations.size(); i++) {
            // 棱
            buildLine(upLoc, locations.get(i), step);
            // 底面
            if (i + 1 == locations.size()) {
                buildLine(locations.get(i), locations.get(0), step);
                break;
            }
            buildLine(locations.get(i), locations.get(i + 1), step);
        }
    }

    public void resetLocations() {
        locations.clear();

        for (double angle = 0; angle <= 360; angle += 360D / side) {
            double radians = Math.toRadians(angle);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            locations.add(getOrigin().clone().add(x, 0, z));
        }
    }

    /**
     * 此方法只用于 Pyramid
     *
     * @param locA 点A
     * @param locB 点B
     * @param step 步长
     */
    private void buildLine(Location locA, Location locB, double step) {
        Vector vectorAB = locB.clone().subtract(locA).toVector();
        double vectorLength = vectorAB.length();
        vectorAB.normalize();
        for (double i = 0; i < vectorLength; i += step) {
            spawnParticle(locA.clone().add(vectorAB.clone().multiply(i)));
        }
    }
}
