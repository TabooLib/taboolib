package io.izzel.taboolib.module.effect.pobject.equation;

import io.izzel.taboolib.module.effect.pobject.ParticleObject;
import org.bukkit.Location;

import java.util.function.Function;

/**
 * 表示一个普通方程渲染器
 *
 * @author Zoyn
 */
public class GeneralEquationRenderer extends ParticleObject {

    private final Function<Double, Double> function;
    private Location origin;
    private double minX;
    private double maxX;
    private double dx;

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function) {
        this(origin, function, -5D, 5D);
    }

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function, double minX, double maxX) {
        this(origin, function, minX, maxX, 0.1);
    }

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function, double minX, double maxX, double dx) {
        this.origin = origin;
        this.function = function;
        this.minX = minX;
        this.maxX = maxX;
        this.dx = dx;
    }

    @Override
    public void show() {
        for (double x = minX; x < maxX; x += dx) {
            spawnParticle(origin.clone().add(x, function.apply(x), 0));
//            origin.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, origin.clone().add(x, function.apply(x), 0), 1);
        }
    }

    public Location getOrigin() {
        return origin;
    }

    public GeneralEquationRenderer setOrigin(Location origin) {
        this.origin = origin;
        return this;
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
