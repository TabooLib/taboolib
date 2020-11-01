package io.izzel.taboolib.effect.pobject.equation;

import io.izzel.taboolib.effect.pobject.ParticleObject;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.function.Function;

public class ParametricEquationRenderer extends ParticleObject {

    private final Function<Double, Double> xFunction;
    private final Function<Double, Double> yFunction;
    private final Function<Double, Double> zFunction;
    private Location origin;
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
    public ParametricEquationRenderer(Location origin, Function<Double, Double> xFunction, Function<Double, Double> yFunction) {
        this(origin, xFunction, yFunction, theta -> 0D, 0D, 360D);
    }

    /**
     * 参数方程渲染器
     *
     * @param origin    原点
     * @param xFunction x函数
     * @param yFunction y函数
     * @param zFunction z函数
     */
    public ParametricEquationRenderer(Location origin, Function<Double, Double> xFunction, Function<Double, Double> yFunction, Function<Double, Double> zFunction) {
        this(origin, xFunction, yFunction, zFunction, 0D, 360D);
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
    public ParametricEquationRenderer(Location origin, Function<Double, Double> xFunction, Function<Double, Double> yFunction, Function<Double, Double> zFunction, double minT, double maxT) {
        this(origin, xFunction, yFunction, zFunction, minT, maxT, 1D);
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
    public ParametricEquationRenderer(Location origin, Function<Double, Double> xFunction, Function<Double, Double> yFunction, Function<Double, Double> zFunction, double minT, double maxT, double dT) {
        this.origin = origin;
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

            origin.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, origin.clone().add(x, y, z), 1);
        }
    }

    public Location getOrigin() {
        return origin;
    }

    public ParametricEquationRenderer setOrigin(Location origin) {
        this.origin = origin;
        return this;
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
