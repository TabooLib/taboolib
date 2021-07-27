package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

import java.util.function.Function;

/**
 * 表示一个参数方程渲染器
 *
 * @author Zoyn
 */
@Isolated
public class ParametricEquationRenderer extends ParticleObj {

    private final Function<Double, Double> xFunction;
    private final Function<Double, Double> yFunction;
    private final Function<Double, Double> zFunction;
    private double minT;
    private double maxT;
    private double dt;

    /**
     * 参数方程渲染器, 自动将z方程变为0
     *
     * @param origin    原点
     * @param xFunction x函数
     * @param yFunction y函数
     */
    public ParametricEquationRenderer(Location origin, Function<Double, Double> xFunction, Function<Double, Double> yFunction, ParticleSpawner spawner) {
        this(origin, xFunction, yFunction, theta -> 0D, 0D, 360D, spawner);
    }

    /**
     * 参数方程渲染器
     *
     * @param origin    原点
     * @param xFunction x函数
     * @param yFunction y函数
     * @param zFunction z函数
     */
    public ParametricEquationRenderer(Location origin, Function<Double, Double> xFunction, Function<Double, Double> yFunction, Function<Double, Double> zFunction, ParticleSpawner spawner) {
        this(origin, xFunction, yFunction, zFunction, 0D, 360D, spawner);
    }

    /**
     * 参数方程渲染器
     *
     * @param origin    原点
     * @param xFunction x函数
     * @param yFunction y函数
     * @param zFunction z函数
     * @param minT      自变量最小值
     * @param maxT      自变量最大值
     */
    public ParametricEquationRenderer(Location origin, Function<Double, Double> xFunction, Function<Double, Double> yFunction, Function<Double, Double> zFunction, double minT, double maxT, ParticleSpawner spawner) {
        this(origin, xFunction, yFunction, zFunction, minT, maxT, 1D, spawner);
    }

    /**
     * 参数方程渲染器
     *
     * @param origin    原点
     * @param xFunction x函数
     * @param yFunction y函数
     * @param zFunction z函数
     * @param minT      自变量最小值
     * @param maxT      自变量最大值
     * @param dT        每次自变量所增加的量
     */
    public ParametricEquationRenderer(Location origin, Function<Double, Double> xFunction, Function<Double, Double> yFunction, Function<Double, Double> zFunction, double minT, double maxT, double dT, ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        this.xFunction = xFunction;
        this.yFunction = yFunction;
        this.zFunction = zFunction;
        this.minT = minT;
        this.maxT = maxT;
        this.dt = dT;
    }

    @Override
    public void show() {
        for (double t = minT; t < maxT; t += dt) {
            double x = xFunction.apply(t);
            double y = yFunction.apply(t);
            double z = zFunction.apply(t);
            spawnParticle(getOrigin().clone().add(x, y, z));
        }
    }

    public double getMinT() {
        return minT;
    }

    public ParametricEquationRenderer setMinT(double minT) {
        this.minT = minT;
        return this;
    }

    public double getMaxT() {
        return maxT;
    }

    public ParametricEquationRenderer setMaxT(double maxT) {
        this.maxT = maxT;
        return this;
    }

    public double getDt() {
        return dt;
    }

    public ParametricEquationRenderer setDt(double dt) {
        this.dt = dt;
        return this;
    }
}
