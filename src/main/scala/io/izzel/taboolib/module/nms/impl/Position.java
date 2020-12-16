package io.izzel.taboolib.module.nms.impl;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

/**
 * Position 映射
 *
 * @Author sky
 * @Since 2020-01-14 22:02
 */
public class Position {

    private int x, y, z;

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public static Position at(Location location) {
        return new Position(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position position = (Position) o;
        return getX() == position.getX() &&
                getY() == position.getY() &&
                getZ() == position.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getZ());
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
