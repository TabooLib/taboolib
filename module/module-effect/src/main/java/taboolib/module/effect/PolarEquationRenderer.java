package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

import java.util.function.Function;

/**
 * 表示一个极坐标方程渲染器
 *
 * @author Zoyn
 */
@Isolated
public class PolarEquationRenderer extends ParticleObj {

    private final Function<Double, Double> function;
    private double minTheta;
    private double maxTheta;
    private double dTheta;

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
    public void show() {
        for (double theta = minTheta; theta < maxTheta; theta += dTheta) {
            double rho = function.apply(theta);
            double x = rho * Math.cos(theta);
            double y = rho * Math.sin(theta);
            spawnParticle(getOrigin().clone().add(x, y, 0));
        }
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
