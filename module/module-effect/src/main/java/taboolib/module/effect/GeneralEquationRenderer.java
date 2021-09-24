package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

import java.util.function.Function;

/**
 * 表示一个普通方程渲染器
 *
 * @author Zoyn
 */
@Isolated
public class GeneralEquationRenderer extends ParticleObj {

    private final Function<Double, Double> function;
    private double minX;
    private double maxX;
    private double dx;

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
    public void show() {
        for (double x = minX; x < maxX; x += dx) {
            spawnParticle(getOrigin().clone().add(x, function.apply(x), 0));
        }
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
