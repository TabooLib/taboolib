package taboolib.module.effect.renderer;

import kotlin.Unit;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.common.util.Location;
import taboolib.common.util.Vector;
import taboolib.module.effect.ParticleObj;
import taboolib.module.effect.ParticleSpawner;
import taboolib.module.effect.Playable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表示一个普通方程渲染器
 *
 * @author Zoyn
 */
public class GeneralEquationRenderer extends ParticleObj implements Playable {

    private final Function<Double, Double> function;
    private double minX;
    private double maxX;
    private double dx;
    private double currentX;

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function, ParticleSpawner spawner) {
        this(origin, function, -5D, 5D, spawner);
    }

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function, double minX, double maxX, ParticleSpawner spawner) {
        this(origin, function, minX, maxX, 0.1, spawner);
    }

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function, double minX, double maxX, double dx, ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        this.function = function;
        this.minX = minX;
        this.maxX = maxX;
        this.dx = dx;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();
        for (double x = minX; x < maxX; x += dx) {
            points.add(getOrigin().clone().add(x, function.apply(x), 0));
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
        for (double x = minX; x < maxX; x += dx) {
            spawnParticle(getOrigin().clone().add(x, function.apply(x), 0));
        }
    }

    @Override
    public void play() {
        ExecutorKt.submit(false, false, 0, getPeriod(), task -> {
            // 进行关闭
            if (currentX > maxX) {
                task.cancel();
            }
            currentX += dx;
            spawnParticle(getOrigin().clone().add(currentX, function.apply(currentX), 0));
            return Unit.INSTANCE;
        });
    }

    @Override
    public void playNextPoint() {
        // 进行关闭
        if (currentX > maxX) {
            currentX = minX;
        }
        currentX += dx;
        spawnParticle(getOrigin().clone().add(currentX, function.apply(currentX), 0));
    }

    public double getMinX() {
        return minX;
    }

    public GeneralEquationRenderer setMinX(double minX) {
        this.minX = minX;
        return this;
    }

    public double getMaxX() {
        return maxX;
    }

    public GeneralEquationRenderer setMaxX(double maxX) {
        this.maxX = maxX;
        return this;
    }

    public double getDx() {
        return dx;
    }

    public GeneralEquationRenderer setDx(double dx) {
        this.dx = dx;
        return this;
    }
}
