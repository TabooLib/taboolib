package taboolib.module.effect.shape;

import taboolib.common.util.Location;
import taboolib.module.effect.ParticleObj;
import taboolib.module.effect.ParticleSpawner;
import taboolib.module.effect.utils.LocationUtils;

public class Lotus extends ParticleObj {

    public Lotus(Location origin, long period, ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        setPeriod(period);
    }

    @Override
    public void show() {
        // 外围花瓣
        for (double t = -0.15D; t <= 0.15D; t += 0.005D) {
            double x = 5 * Math.sin(t) * Math.cos(t) * Math.log(Math.abs(t));
            double y = 5 * Math.sqrt(Math.abs(t)) * Math.cos(t);
            y -= 5;

            Location spawn = getOrigin().clone().add(x, 0, y);
            for (int i = 0; i <= 8; i++) {
                Location temp = LocationUtils.rotateLocationAboutPoint(spawn, 360D / 8D * i, getOrigin());
                spawnParticle(temp);
            }

            spawnParticle(spawn);
        }

        // 内圈花瓣
        for (double t = -0.2D; t <= 0.2D; t += 0.01D) {
            double x = 3 * Math.sin(t) * Math.cos(t) * Math.log(Math.abs(t));
            double y = 3 * Math.sqrt(Math.abs(t)) * Math.cos(t);
            y -= 3.65;

            Location spawn = getOrigin().clone().add(x, 0, y);
            spawn = LocationUtils.rotateLocationAboutPoint(spawn, 22D, getOrigin());

            for (int i = 0; i <= 8; i++) {
                Location temp = LocationUtils.rotateLocationAboutPoint(spawn, 360D / 8D * i, getOrigin());
                spawnParticle(temp);
            }

            spawnParticle(spawn);
        }

        // 最外层小花瓣
        for (double t = -0.1D; t <= 0.1D; t += 0.01D) {
            double x = 2 * Math.sin(t) * Math.cos(t) * Math.log(Math.abs(t));
            double y = 2 * Math.sqrt(Math.abs(t)) * Math.cos(t);
            y -= 4.6;

            Location spawn = getOrigin().clone().add(x, 0, y);
            spawn = LocationUtils.rotateLocationAboutPoint(spawn, 22D, getOrigin());

            for (int i = 0; i <= 8; i++) {
                Location temp = LocationUtils.rotateLocationAboutPoint(spawn, 360D / 8D * i, getOrigin());
                spawnParticle(temp);
            }

            spawnParticle(spawn);
        }

    }


}
