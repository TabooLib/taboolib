package taboolib.module.effect.shape;

import taboolib.common.util.Location;
import taboolib.common.util.Vector;
import taboolib.module.effect.ParticleObj;
import taboolib.module.effect.ParticleSpawner;

public class Grid extends ParticleObj {

    private final double gridLength;
    private Location minimumLocation;
    private Location maximumLocation;
    private boolean isXDimension = false;
    private boolean isYDimension = false;

    public Grid(Location minimumLocation, Location maximumLocation , ParticleSpawner spawner) {
        this(minimumLocation, maximumLocation, 1.2D , 20L , spawner);
    }

    public Grid(Location minimumLocation, Location maximumLocation, double gridLength , long period , ParticleSpawner spawner) {
        super(spawner);
        this.minimumLocation = minimumLocation;
        this.maximumLocation = maximumLocation;
        // 平面检查
        if (minimumLocation.getBlockX() != maximumLocation.getBlockX()) {
            if (minimumLocation.getBlockZ() != maximumLocation.getBlockZ()) {
                if (minimumLocation.getBlockY() != maximumLocation.getBlockY()) {
                    throw new IllegalArgumentException("请将两点设定在X平面, Y平面或Z平面上(即一个方块的面上)");
                }
            }
        }
        if (minimumLocation.getBlockX() == maximumLocation.getBlockX()) {
            isXDimension = false;
        }
        if (minimumLocation.getBlockY() == maximumLocation.getBlockY()) {
            isYDimension = true;
        }
        if (minimumLocation.getBlockZ() == maximumLocation.getBlockZ()) {
            isXDimension = true;
        }

        this.gridLength = gridLength;
        setPeriod(period);
    }

    @Override
    public void show() {
        // 为防止给定的最小和最高点出现反向的情况, 这里做了个查找操作
        Location minLocation = findMinimumLocation();
        Location maxLocation = findMaximumLocation();

        double height;
        double width;

        // 在Y平面下有点不一样
        if (isYDimension) {
            height = Math.abs(minLocation.getX() - maxLocation.getX());
            width = Math.abs(minLocation.getZ() - maxLocation.getZ());
        } else {
            height = Math.abs(maximumLocation.getY() - minimumLocation.getY());
            if (isXDimension) {
                width = Math.abs(maximumLocation.getX() - minimumLocation.getX());
            } else {
                width = Math.abs(maximumLocation.getZ() - minimumLocation.getZ());
            }
        }
        int heightSideLine = (int) (height / gridLength);
        int widthSideLine = (int) (width / gridLength);

        if (isYDimension) {
            for (int i = 1; i <= heightSideLine; i++) {
                Vector vector = maxLocation.clone().subtract(minLocation).toVector();
                vector.setZ(0).normalize();

                Location start = minLocation.clone().add(0, 0, i * gridLength);
                for (double j = 0; j < width; j += 0.2) {
                    spawnParticle(start.clone().add(vector.clone().multiply(j)));

                }
            }

            for (int i = 1; i <= widthSideLine; i++) {
                Vector vector = maxLocation.clone().subtract(minLocation).toVector();
                vector.setX(0).normalize();
                Location start = minLocation.clone().add(i * gridLength, 0, 0);

                for (double j = 0; j < height; j += 0.2) {
                    spawnParticle(start.clone().add(vector.clone().multiply(j)));
                }
            }
            return;
        }

        for (int i = 1; i <= heightSideLine; i++) {
            Vector vector = maxLocation.clone().subtract(minLocation).toVector();
            vector.setY(0).normalize();

            Location start = minLocation.clone().add(0, i * gridLength, 0);
            for (double j = 0; j < width; j += 0.2) {
                spawnParticle(start.clone().add(vector.clone().multiply(j)));
            }
        }

        for (int i = 1; i <= widthSideLine; i++) {
            Vector vector = maxLocation.clone().subtract(minLocation).toVector();
            Location start;
            if (isXDimension) {
                vector.setX(0).normalize();
                start = minLocation.clone().add(i * gridLength, 0, 0);
            } else {
                vector.setZ(0).normalize();
                start = minLocation.clone().add(0, 0, i * gridLength);
            }

            for (double j = 0; j < height; j += 0.2) {
                spawnParticle(start.clone().add(vector.clone().multiply(j)));
            }
        }
    }

    private Location findMinimumLocation() {
        double minX = Math.min(minimumLocation.getX(), maximumLocation.getX());
        double minY = Math.min(minimumLocation.getY(), maximumLocation.getY());
        double minZ = Math.min(minimumLocation.getZ(), maximumLocation.getZ());

        return new Location(minimumLocation.getWorld(), minX, minY, minZ);
    }

    private Location findMaximumLocation() {
        double maxX = Math.max(minimumLocation.getX(), maximumLocation.getX());
        double maxY = Math.max(minimumLocation.getY(), maximumLocation.getY());
        double maxZ = Math.max(minimumLocation.getZ(), maximumLocation.getZ());

        return new Location(minimumLocation.getWorld(), maxX, maxY, maxZ);
    }

    public Location getMinimumLocation() {
        return minimumLocation;
    }

    public void setMinimumLocation(Location minimumLocation) {
        this.minimumLocation = minimumLocation;
    }

    public Location getMaximumLocation() {
        return maximumLocation;
    }

    public void setMaximumLocation(Location maximumLocation) {
        this.maximumLocation = maximumLocation;
    }

}
