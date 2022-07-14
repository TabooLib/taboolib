package io.izzel.taboolib.cronus.bukkit;

import org.bukkit.Bukkit;

import java.util.Arrays;

/**
 * @author 坏黑
 * @since 2019-05-24 14:09
 */
public class Location {

    private final Mode mode;
    private final org.bukkit.Location[] area;
    private final org.bukkit.Location[] points;
    private int range;

    public Location(Mode mode, org.bukkit.Location[] area, org.bukkit.Location[] points) {
        this.mode = mode;
        this.area = area;
        this.points = points;
    }

    public Location(Mode mode, org.bukkit.Location[] points, int range) {
        this.mode = mode;
        this.area = new org.bukkit.Location[0];
        this.points = points;
        this.range = range;
    }

    public org.bukkit.Location toBukkit() {
        return points != null && points.length > 0 ? points[0] : new org.bukkit.Location(Bukkit.getWorlds().get(0), 0, 0, 0);
    }

    public boolean isBukkit() {
        try {
            return toBukkit() != null;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public boolean isSelect(org.bukkit.Location locationA, org.bukkit.Location locationB) {
        return locationA.getWorld().equals(locationB.getWorld()) && locationA.getX() == locationB.getX() && locationA.getY() == locationB.getY() && locationA.getZ() == locationB.getZ();
    }

    public boolean inSelect(org.bukkit.Location location) {
        if (!isSelectWorld(location)) {
            return false;
        }
        switch (mode) {
            case AREA:
                return area != null && location.toVector().isInAABB(area[0].toVector(), area[1].toVector());
            case POINT:
                return points != null && Arrays.stream(points).anyMatch(p -> isSelect(p, location));
            case RANGE:
                return points != null && toBukkit().distance(location) <= range;
            default:
                return false;
        }
    }

    public boolean isSelectWorld(org.bukkit.Location location) {
        if (mode == Mode.AREA) {
            return area != null && location.getWorld().equals(area[0].getWorld());
        }
        return points != null && Arrays.stream(points).anyMatch(p -> p.getWorld().equals(location.getWorld()));
    }

    @Override
    public String toString() {
        return "Location{" +
                "mode=" + mode +
                ", area=" + Arrays.toString(area) +
                ", points=" + Arrays.toString(points) +
                ", range=" + range +
                '}';
    }

    public Mode getMode() {
        return mode;
    }

    public org.bukkit.Location[] getArea() {
        return area;
    }

    public org.bukkit.Location[] getPoints() {
        return points;
    }

    public enum Mode {

        AREA, POINT, RANGE
    }
}
