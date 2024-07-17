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
 * 表示一个极坐标方程渲染器
 *
 * @author Zoyn
 */
public class PolarEquationRenderer extends ParticleObj implements Playable {

    private final Function<Double, Double> function;
    private double minTheta;
    private double maxTheta;
    private double dTheta;
    private double currentTheta;

    /**
     * 极坐标渲染器
     *
     * @param origin   原点
     * @param function 极坐标方程
     */
    public PolarEquationRenderer(Location origin, Function<Double, Double> function, ParticleSpawner spawner) {
        this(origin, function, 0D, 360D, 1D, spawner);
    }

    /**
     * 极坐标渲染器
     *
     * @param origin   原点
     * @param function 极坐标方程
     * @param minTheta 自变量最小值
     * @param maxTheta 自变量最大值
     * @param dTheta   每次自变量所增加的量
     */
    public PolarEquationRenderer(Location origin, Function<Double, Double> function, double minTheta, double maxTheta, double dTheta, ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        this.function = function;
        this.minTheta = minTheta;
        this.maxTheta = maxTheta;
        this.dTheta = dTheta;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();
        for (double theta = minTheta; theta < maxTheta; theta += dTheta) {
            double rho = function.apply(theta);
            double x = rho * Math.cos(theta);
            double y = rho * Math.sin(theta);
            points.add(getOrigin().clone().add(x, y, 0));
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
        for (double theta = minTheta; theta < maxTheta; theta += dTheta) {
            double rho = function.apply(theta);
            double x = rho * Math.cos(theta);
            double y = rho * Math.sin(theta);
            spawnParticle(getOrigin().clone().add(x, y, 0));
        }
    }

    @Override
    public void play() {
        ExecutorKt.submit(false, false, 0, getPeriod(), task -> {
            // 进行关闭
            if (currentTheta > maxTheta) {
                task.cancel();
            }
            currentTheta += dTheta;

            double rho = function.apply(currentTheta);
            double x = rho * Math.cos(currentTheta);
            double y = rho * Math.sin(currentTheta);
            spawnParticle(getOrigin().clone().add(x, y, 0));
            return Unit.INSTANCE;
        });
    }

    @Override
    public void playNextPoint() {
        // 进行关闭
        if (currentTheta > maxTheta) {
            currentTheta = minTheta;
        }
        currentTheta += dTheta;

        double rho = function.apply(currentTheta);
        double x = rho * Math.cos(currentTheta);
        double y = rho * Math.sin(currentTheta);
        spawnParticle(getOrigin().clone().add(x, y, 0));
    }

    public double getMinTheta() {
        return minTheta;
    }

    public PolarEquationRenderer setMinTheta(double minTheta) {
        this.minTheta = minTheta;
        return this;
    }

    public double getMaxTheta() {
        return maxTheta;
    }

    public PolarEquationRenderer setMaxTheta(double maxTheta) {
        this.maxTheta = maxTheta;
        return this;
    }

    public double getDTheta() {
        return dTheta;
    }

    public PolarEquationRenderer setDTheta(double dTheta) {
        this.dTheta = dTheta;
        return this;
    }
}
