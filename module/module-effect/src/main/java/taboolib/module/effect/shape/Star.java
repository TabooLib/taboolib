package taboolib.module.effect.shape;

import kotlin.Unit;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.common.util.Location;
import taboolib.common.util.Vector;
import taboolib.module.effect.ParticleObj;
import taboolib.module.effect.ParticleSpawner;
import taboolib.module.effect.Playable;
import taboolib.module.effect.utils.VectorUtils;

/**
 * 表示一个星星
 *
 * @author Zoyn
 */
public class Star extends ParticleObj implements Playable {

    private final double radius;
    private final double step;

    private final double length;
    private int currentSide = 1;
    private double currentStep = 0;

    private Vector changeableStart;
    private Location changableEnd;

    public Star(Location origin , ParticleSpawner spawner) {
        this(origin, 1, 0.05, 20L ,spawner);
    }

    public Star(Location origin, double radius, double step , long period , ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        setPeriod(period);
        this.radius = radius;
        this.step = step;

        // 每条线的长度
        this.length = Math.cos(Math.toRadians(36)) * radius * 2;

        changeableStart = new Vector(1, 0, 0);
        // 转弧度制
        final double radians = Math.toRadians(72 * 2);
        final double x = radius * Math.cos(radians);
        final double y = 0D;
        final double z = radius * Math.sin(radians);
        changableEnd = getOrigin().clone().add(x, y, z);
    }

    @Override
    public void show() {
        Vector START = new Vector(1, 0, 0);
        // 转弧度制
        double radians = Math.toRadians(72 * 2);
        double x = radius * Math.cos(radians);
        double y = 0D;
        double z = radius * Math.sin(radians);
        Location end = getOrigin().clone().add(x, y, z);

        for (int i = 1; i <= 5; i++) {
            for (double j = 0; j < length; j += step) {
                Vector vectorTemp = START.clone().multiply(j);
                Location spawnLocation = end.clone().add(vectorTemp);

                spawnParticle(spawnLocation);
            }
            Vector vectorTemp = START.clone().multiply(length);
            end = end.clone().add(vectorTemp);

            VectorUtils.rotateAroundAxisY(START, -144);
        }
    }

    @Override
    public void play() {
        ExecutorKt.submit(false , false , 0 , getPeriod() , null , platformTask -> {
            final Vector START = new Vector(1, 0, 0);
            // 转弧度制
            final double radians = Math.toRadians(72 * 2);
            final double x = radius * Math.cos(radians);
            final double y = 0D;
            final double z = radius * Math.sin(radians);
            Location end = getOrigin().clone().add(x, y, z);
            // 进行关闭
            if (currentSide >= 6) {
                platformTask.cancel();
            }
            if (currentStep > length) {
                // 切换到下一条边开始
                currentSide += 1;
                currentStep = 0;

                Vector vectorTemp = START.clone().multiply(length);
                end = end.clone().add(vectorTemp);

                VectorUtils.rotateAroundAxisY(START, -144);
            }
            Vector vectorTemp = START.clone().multiply(currentStep);
            Location spawnLocation = end.clone().add(vectorTemp);

            spawnParticle(spawnLocation);
            currentStep += step;
            return Unit.INSTANCE;
        });
    }

    @Override
    public void playNextPoint() {
        Vector vectorTemp = changeableStart.clone().multiply(currentStep);
        Location spawnLocation = changableEnd.clone().add(vectorTemp);

        spawnParticle(spawnLocation);
        currentStep += step;
        if (currentStep > length) {
            // 切换到下一条边开始
            currentSide += 1;
            currentStep = 0;

            vectorTemp = changeableStart.clone().multiply(length);
            changableEnd = changableEnd.clone().add(vectorTemp);

            VectorUtils.rotateAroundAxisY(changeableStart, -144);
        }

        // 重置
        if (currentSide >= 6) {
            currentSide = 1;
            currentStep = 0;
            changeableStart = new Vector(1, 0, 0);
            // 转弧度制
            final double radians = Math.toRadians(72 * 2);
            final double x = radius * Math.cos(radians);
            final double y = 0D;
            final double z = radius * Math.sin(radians);
            changableEnd = getOrigin().clone().add(x, y, z);
        }
    }
}
