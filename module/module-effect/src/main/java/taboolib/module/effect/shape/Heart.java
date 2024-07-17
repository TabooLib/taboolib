package taboolib.module.effect.shape;

import kotlin.Unit;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.common.util.Location;
import taboolib.common.util.Vector;
import taboolib.module.effect.ParticleObj;
import taboolib.module.effect.ParticleSpawner;
import taboolib.module.effect.Playable;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一颗心
 *
 * @author Zoyn IceCold
 */
public class Heart extends ParticleObj implements Playable {

    private double xScaleRate;
    private double yScaleRate;
    /**
     * 表示步进的程度
     */
    private double step = 0.001D;
    private double currentT = -1.0D;


    /**
     * 构造一个小心心
     *
     * @param origin 原点
     */
    public Heart(Location origin, ParticleSpawner spawner) {
        this(1, 1, origin , 20L , spawner);
    }

    /**
     * 构造一个心形线
     *
     * @param xScaleRate X轴缩放比率
     * @param yScaleRate Y轴缩放比率
     * @param origin     原点
     */
    public Heart(double xScaleRate, double yScaleRate, Location origin ,long period, ParticleSpawner spawner) {
        super(spawner);
        this.xScaleRate = xScaleRate;
        this.yScaleRate = yScaleRate;
        setOrigin(origin);
        setPeriod(period);
    }

    public double getXScaleRate() {
        return xScaleRate;
    }

    public void setXScaleRate(double xScaleRate) {
        this.xScaleRate = xScaleRate;
    }

    public double getYScaleRate() {
        return yScaleRate;
    }

    public void setYScaleRate(double yScaleRate) {
        this.yScaleRate = yScaleRate;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();
        for (double t = -1.0D; t <= 1.0D; t += step) {
            double x = xScaleRate * Math.sin(t) * Math.cos(t) * Math.log(Math.abs(t));
            double y = yScaleRate * Math.sqrt(Math.abs(t)) * Math.cos(t);

            Location showLocation = getOrigin().clone().add(x, 0, y);
            if (hasMatrix()) {
                Vector vector = new Vector(x, 0, y);
                Vector changed = getMatrix().applyVector(vector);

                showLocation = getOrigin().clone().add(changed);
            }

            showLocation.add(getIncrementX(), getIncrementY(), getIncrementZ());
            points.add(showLocation);
        }
        return points;
    }

    @Override
    public void show() {
        for (double t = -1.0D; t <= 1.0D; t += step) {
            double x = xScaleRate * Math.sin(t) * Math.cos(t) * Math.log(Math.abs(t));
            double y = yScaleRate * Math.sqrt(Math.abs(t)) * Math.cos(t);

            spawnParticle(getOrigin().clone().add(x, 0, y));

        }
    }

    @Override
    public void play() {
        ExecutorKt.submit(false , false , 0 , getPeriod() , platformTask -> {

            if (currentT > 1.0D) {
                platformTask.cancel();
            }
            currentT += step;
            double x = xScaleRate * Math.sin(currentT) * Math.cos(currentT) * Math.log(Math.abs(currentT));
            double y = yScaleRate * Math.sqrt(Math.abs(currentT)) * Math.cos(currentT);

            spawnParticle(getOrigin().clone().add(x, 0, y));

            return Unit.INSTANCE;
        });
    }

    @Override
    public void playNextPoint() {
        currentT += step;
        double x = xScaleRate * Math.sin(currentT) * Math.cos(currentT) * Math.log(Math.abs(currentT));
        double y = yScaleRate * Math.sqrt(Math.abs(currentT)) * Math.cos(currentT);

        spawnParticle(getOrigin().clone().add(x, 0, y));

        if (currentT > 1.0D) {
            currentT = -1.0D;
        }
    }
}