package taboolib.module.effect;

import kotlin.Unit;
import taboolib.common.Isolated;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.common.util.Location;

/**
 * 表示一个星形线
 *
 * @author Zoyn
 */
@Isolated
public class Astroid extends ParticleObj implements Playable {

    private double radius;
    private double step;

    private double currentT = 0D;

    /**
     * 构造一个星形线
     *
     * @param origin 原点
     */
    public Astroid(Location origin, ParticleSpawner spawner) {
        this(1D, origin, spawner);
    }

    /**
     * 构造一个星形线
     *
     * @param radius 半径
     * @param origin 原点
     */
    public Astroid(double radius, Location origin, ParticleSpawner spawner) {
        super(spawner);
        this.radius = radius;
        setOrigin(origin);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    @Override
    public void show() {
        for (double t = 0.0D; t < 360.0D; t++) {
            double radians = Math.toRadians(t);
            // 计算公式
            double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
            double z = Math.pow(this.radius * Math.sin(radians), 3.0D);
            spawnParticle(getOrigin().clone().add(x, 0, z));
        }
    }

    @Override
    public void play() {
        ExecutorKt.submit(false, false, 0, getPeriod(), null, task -> {
            // 进行关闭
            // 重置
            if (currentT > 360D) {
                task.cancel();
                return Unit.INSTANCE;
            }
            currentT += step;
            double radians = Math.toRadians(currentT);
            // 计算公式
            double x = Math.pow(getRadius() * Math.cos(radians), 3.0D);
            double z = Math.pow(getRadius() * Math.sin(radians), 3.0D);

            spawnParticle(getOrigin().clone().add(x, 0, z));
            return Unit.INSTANCE;
        });
    }

    @Override
    public void playNextPoint() {
        currentT += step;
        double radians = Math.toRadians(currentT);
        // 计算公式
        double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
        double z = Math.pow(this.radius * Math.sin(radians), 3.0D);

        spawnParticle(getOrigin().clone().add(x, 0, z));
        // 重置
        if (currentT > 360D) {
            currentT = 0D;
        }
    }
}
