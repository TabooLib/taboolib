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
import java.util.stream.Collectors;

/**
 * 表示一条n阶贝塞尔曲线
 * <p>给定 n + 1 点, 绘制一条平滑的曲线</p>
 *
 * @author Zoyn
 */
public class NRankBezierCurve extends ParticleObj implements Playable {

    /**
     * 用于保存将要播放的粒子的点位
     */
    private final List<Location> points = new ArrayList<>();
    private final double step;
    /**
     * 用于计算贝塞尔曲线上的点
     */
    private final List<Location> locations;
    private int currentSample = 0;

    /**
     * 构造一个N阶贝塞尔曲线
     *
     * @param locations 所有的点
     * @param spawner   粒子生成器
     */
    public NRankBezierCurve(List<Location> locations, ParticleSpawner spawner) {
        this(locations, 0.05D, spawner);
    }

    /**
     * 构造一个N阶贝塞尔曲线
     *
     * @param locations 所有的点
     * @param step      T的步进数
     */
    public NRankBezierCurve(List<Location> locations, double step, ParticleSpawner spawner) {
        super(spawner);
        this.locations = locations;
        this.step = step;
        resetLocation();
    }

    private static Location calculateCurve(List<Location> locList, double t) {
        if (locList.size() == 2) {
            return locList.get(0).clone().add(locList.get(1).clone().subtract(locList.get(0)).toVector().multiply(t));
        }

        List<Location> locListTemp = new ArrayList<>();
        for (int i = 0; i < locList.size(); i++) {
            if (i + 1 == locList.size()) {
                break;
            }
            Location p0 = locList.get(i);
            Location p1 = locList.get(i + 1);

            // 降阶处理
            locListTemp.add(p0.clone().add(p1.clone().subtract(p0).toVector().multiply(t)));
        }
        return calculateCurve(locListTemp, t);
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();
        for (double t = 0; t < 1; t += step) {
            Location location = calculateCurve(locations, t);
            points.add(location);
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
        points.forEach(loc -> {
            if (loc != null) {
                spawnParticle(loc);
            }
        });
    }

    @Override
    public void play() {
        ExecutorKt.submit(false, false, 0, getPeriod(), task -> {
            // 进行关闭
            if (currentSample + 1 == points.size()) {
                task.cancel();
            }
            currentSample++;

            spawnParticle(points.get(currentSample));
            return Unit.INSTANCE;
        });
    }

    @Override
    public void playNextPoint() {
        if (currentSample + 1 == points.size()) {
            currentSample = 0;
        }
        spawnParticle(points.get(currentSample));
        currentSample++;
    }

    public void resetLocation() {
        points.clear();

        for (double t = 0; t < 1; t += step) {
            Location location = calculateCurve(locations, t);
            points.add(location);
        }
    }
}
