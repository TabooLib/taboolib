package taboolib.module.effect.shape;

import kotlin.Unit;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.common.util.Location;
import taboolib.common.util.Vector;
import taboolib.module.effect.ParticleObj;
import taboolib.module.effect.ParticleSpawner;
import taboolib.module.effect.Playable;

/**
 * 代表一个射线
 *
 * @author Zoyn IceCold
 */
public class Ray extends ParticleObj implements Playable {

    private Vector direction;
    private double maxLength;
    private double step;
    private double range;
    private RayStopType stopType;
    private double length;
    private double currentStep = 0D;

    public Ray(Location origin, Vector direction, double maxLength , ParticleSpawner spawner) {
        this(origin, direction, maxLength, 0.2D , spawner);
    }

    public Ray(Location origin, Vector direction, double maxLength , double step , ParticleSpawner spawner) {
        this(origin, direction, maxLength, step, 0.5D, RayStopType.MAX_LENGTH , 20L , spawner);
    }

    public Ray(Location origin, Vector direction, double maxLength, double step, double range, RayStopType stopType , long period , ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        this.direction = direction;
        this.maxLength = maxLength;
        this.step = step;
        this.range = range;
        this.stopType = stopType;
        setPeriod(period);
    }

    @Override
    public void show() {
        for (double i = 0; i < maxLength; i += step) {
            Vector vectorTemp = direction.clone().multiply(i);
            Location spawnLocation = getOrigin().clone().add(vectorTemp);

            spawnParticle(spawnLocation);

        }
    }

    @Override
    public void play() {
        ExecutorKt.submit(false , false , 0 , getPeriod() ,  platformTask -> {
            // 进行关闭
            if (currentStep > maxLength) {
                platformTask.cancel();
            }
            currentStep += step;
            Vector vectorTemp = direction.clone().multiply(currentStep);
            Location spawnLocation = getOrigin().clone().add(vectorTemp);

            spawnParticle(spawnLocation);
            return Unit.INSTANCE;
        });
    }

    @Override
    public void playNextPoint() {
        currentStep += step;
        Vector vectorTemp = direction.clone().multiply(currentStep);
        Location spawnLocation = getOrigin().clone().add(vectorTemp);

        spawnParticle(spawnLocation);

        if (currentStep > maxLength) {
            currentStep = 0D;
        }
    }

    public Vector getDirection() {
        return direction;
    }

    public Ray setDirection(Vector direction) {
        this.direction = direction;
        return this;
    }

    public double getMaxLength() {
        return maxLength;
    }

    public Ray setMaxLength(double maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public double getStep() {
        return step;
    }

    public Ray setStep(double step) {
        this.step = step;
        return this;
    }

    public double getRange() {
        return range;
    }

    public Ray setRange(double range) {
        this.range = range;
        return this;
    }

    public RayStopType getStopType() {
        return stopType;
    }

    public Ray setStopType(RayStopType stopType) {
        this.stopType = stopType;
        return this;
    }

    public enum RayStopType {
        /**
         * 固定长度(同时也是最大长度)
         */
        MAX_LENGTH,
    }

}
