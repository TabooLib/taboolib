package io.izzel.taboolib.util.lite;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.Reflection;
import io.izzel.taboolib.util.TMap;
import io.izzel.taboolib.util.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * @Author sky
 * @Since 2019-10-06 1:02
 * <p>
 * 部分代码来自
 * CryptoMorin 的 XSeries 项目
 */
public class Effects {

    private Particle particle;
    private Location center;
    private double[] offset = {0, 0, 0};
    private double speed = 0;
    private double range = 0;
    private int count = 0;
    private List<Player> player = Lists.newArrayList();
    private Object data;

    public static Particle parseParticle(String in) {
        return Enums.getIfPresent(Particle.class, in).or(Particle.FLAME);
    }

    public static Effects create(Particle particle, Location center) {
        return new Effects(particle, center);
    }

    public static Effects create(Particle particle, Location center, double offsetX, double offsetY, double offsetZ) {
        return new Effects(particle, center).offset(new double[] {offsetX, offsetY, offsetZ});
    }

    public static Effects create(Particle particle, Location center, double offsetX, double offsetY, double offsetZ, double speed) {
        return new Effects(particle, center).offset(new double[] {offsetX, offsetY, offsetZ}).speed(speed);
    }

    public static Effects create(Particle particle, Location center, double offsetX, double offsetY, double offsetZ, double speed, int count) {
        return new Effects(particle, center).offset(new double[] {offsetX, offsetY, offsetZ}).speed(speed).count(count);
    }

    public static Effects parse(String in) {
        TMap map = TMap.parse(in);
        Effects effects = Effects.create(parseParticle(map.getName()), null);
        for (Map.Entry<String, String> entry : map.getContent().entrySet()) {
            switch (entry.getKey()) {
                case "offset":
                case "o":
                    Double[] offset = Arrays.stream(entry.getValue().split(",")).map(NumberConversions::toDouble).toArray(Double[]::new);
                    effects.offset(new double[] {offset.length > 0 ? offset[0] : 0, offset.length > 1 ? offset[1] : 0, offset.length > 2 ? offset[2] : 0});
                    break;
                case "speed":
                case "s":
                    effects.speed(NumberConversions.toDouble(entry.getValue()));
                    break;
                case "range":
                case "r":
                    effects.range(NumberConversions.toDouble(entry.getValue()));
                    break;
                case "count":
                case "c":
                case "amount":
                case "a":
                    effects.count(NumberConversions.toInt(entry.getValue()));
                    break;
                case "data":
                case "d":
                    String[] data = entry.getValue().split(":");
                    if (effects.particle.getDataType().equals(ItemStack.class)) {
                        effects.data(new ItemStack(Items.asMaterial(data[0]), 1, data.length > 1 ? NumberConversions.toShort(data[1]) : 0));
                    } else if (effects.particle.getDataType().equals(MaterialData.class)) {
                        effects.data(new MaterialData(Items.asMaterial(data[0]), data.length > 1 ? NumberConversions.toByte(data[1]) : 0));
                    } else if (effects.particle == Particle.REDSTONE) {
                        effects.data(new ColorData(Color.fromRGB(NumberConversions.toInt(data[0])), NumberConversions.toInt(data[1])));
                    }
                    break;
            }
        }
        return effects;
    }

    public static void buildLine(Location locA, Location locB, Consumer<Location> action) {
        buildLine(locA, locB, action, 0.25);
    }

    public static void buildLine(Location locA, Location locB, Consumer<Location> action, double interval) {
        Vector vectorAB = locB.clone().subtract(locA).toVector();
        double vectorLength = vectorAB.length();
        vectorAB.normalize();
        for (double i = 0; i < vectorLength; i += interval) {
            action.accept(locA.clone().add(vectorAB.clone().multiply(i)));
        }
    }

    public static void buildPolygon(Location center, double radius, double interval, Consumer<Location> action) {
        for (double i = 0; i < 360; i += interval) {
            double radians = Math.toRadians(i);
            double cos = Math.cos(radians) * radius;
            double sin = Math.sin(radians) * radius;
            action.accept(center.clone().add(cos, 0, sin));
        }
    }

    public static void buildCircle(Location center, double radius, double rate, Consumer<Location> action) {
        double pii = Math.PI * 2;
        double rateDiv = Math.PI / rate;
        for (double theta = 0; theta <= pii; theta += rateDiv) {
            double x = radius * Math.cos(theta);
            double z = radius * Math.sin(theta);
            action.accept(center.clone().add(x, 0, z));
        }
    }


    public static void buildCone(Location center, double height, double radius, double rate, double circleRate, Consumer<Location> action) {
        double radiusDiv = radius / (height / rate);
        for (double i = 0; i < height; i += rate) {
            radius -= radiusDiv;
            buildCircle(center.clone().add(0, i, 0), Math.max(radius, 0), circleRate - i, action);
        }
    }

    public static void buildAtom(Location center, int orbits, double radius, double rate, Consumer<Location> orbit, Consumer<Location> nucleus) {
        double dist = Math.PI / orbits;
        for (double angle = 0; orbits > 0; angle += dist) {
            buildCircle(center, radius, rate, orbit);
            orbits--;
        }
        buildSphere(center, radius / 3, rate / 2, nucleus);
    }

    public static void buildEllipse(Location center, double radius, double otherRadius, double rate, Consumer<Location> action) {
        double pii = Math.PI * 2;
        double rateDiv = Math.PI / rate;
        for (double theta = 0; theta <= pii; theta += rateDiv) {
            double x = radius * Math.cos(theta);
            double y = otherRadius * Math.sin(theta);
            action.accept(center.clone().add(x, y, 0));
        }
    }

    public static void buildInfinity(Location center, double radius, double rate, Consumer<Location> action) {
        double pii = Math.PI * 2;
        double rateDiv = Math.PI / rate;
        for (double i = 0; i < pii; i += rateDiv) {
            double x = Math.sin(i);
            double smooth = Math.pow(x, 2) + 1;
            double curve = radius * Math.cos(i);
            double z = curve / smooth;
            double y = (curve * x) / smooth;
            buildCircle(center.clone().add(x, y, z), 1, rate, action);
        }
    }

    public static void buildCrescent(Location center, double radius, double rate, Consumer<Location> action) {
        double rateDiv = Math.PI / rate;
        for (double theta = Math.toRadians(45); theta <= Math.toRadians(325); theta += rateDiv) {
            double x = Math.cos(theta);
            double z = Math.sin(theta);
            action.accept(center.clone().add(radius * x, 0, radius * z));
            double smallerRadius = radius / 1.3;
            action.accept(center.clone().add(smallerRadius * x + 0.8, 0, smallerRadius * z));
        }
    }

    public static void buildWaveFunction(Location center, double extend, double heightRange, double size, double rate, Consumer<Location> action) {
        double pii = Math.PI * 2;
        double height = heightRange / 2;
        boolean increase = true;
        double increaseRandomizer = Numbers.getRandomDouble(heightRange / 2, heightRange);
        double rateDiv = Math.PI / rate;
        size *= pii;
        for (double x = 0; x <= size; x += rateDiv) {
            double xx = extend * x;
            double y1 = Math.sin(x);
            if (y1 == 1) {
                increase = !increase;
                if (increase) {
                    increaseRandomizer = Numbers.getRandomDouble(heightRange / 2, heightRange);
                } else {
                    increaseRandomizer = Numbers.getRandomDouble(-heightRange, -heightRange / 2);
                }
            }
            height += increaseRandomizer;
            for (double z = 0; z <= size; z += rateDiv) {
                double y2 = Math.cos(z);
                double yy = height * y1 * y2;
                double zz = extend * z;
                action.accept(center.clone().add(xx, yy, zz));
            }
        }
    }

    public static void buildCylinder(Location center, double height, double radius, double rate, double interval, Consumer<Location> action) {
        double rateDiv = Math.PI / rate;
        for (double theta = 0; theta <= Math.PI; theta += rateDiv) {
            double x = radius * Math.cos(theta);
            double z = radius * Math.sin(theta);
            action.accept(center.clone().add(x, 0, z));
            action.accept(center.clone().add(-x, 0, -z));
            action.accept(center.clone().add(x, height, z));
            action.accept(center.clone().add(-x, height, -z));
            Location point1 = center.clone().add(x, 0, z);
            Location point2 = center.clone().add(-x, 0, -z);
            buildLine(point1, point2, action, interval);
            Location point21 = center.clone().add(x, height, z);
            Location point22 = center.clone().add(-x, height, -z);
            buildLine(point21, point22, action, interval);
            buildLine(point1, point21, action, interval);
            buildLine(point2, point22, action, interval);
        }
    }

    public static void buildSphere(Location center, double radius, double rate, Consumer<Location> action) {
        double pii = Math.PI * 2;
        double rateDiv = Math.PI / rate;
        for (double phi = 0; phi <= Math.PI; phi += rateDiv) {
            double y1 = radius * Math.cos(phi);
            double y2 = radius * Math.sin(phi);
            for (double theta = 0; theta <= pii; theta += rateDiv) {
                double x = Math.cos(theta) * y2;
                double z = Math.sin(theta) * y2;
                action.accept(center.clone().add(x, y1, z));
            }
        }
    }

    public static void buildSphereSpike(Location center, double radius, double rate, int chance, double minRandomDistance, double maxRandomDistance, double interval, Consumer<Location> action) {
        double pii = Math.PI * 2;
        double rateDiv = Math.PI / rate;
        for (double phi = 0; phi <= Math.PI; phi += rateDiv) {
            double y = radius * Math.cos(phi);
            double sinPhi = radius * Math.sin(phi);
            for (double theta = 0; theta <= pii; theta += rateDiv) {
                double x = Math.cos(theta) * sinPhi;
                double z = Math.sin(theta) * sinPhi;
                if (chance == 0 || Numbers.getRandomInteger(0, chance) == 1) {
                    Location start = center.clone().add(x, y, z);
                    Vector endV = start.clone().subtract(center).toVector().multiply(Numbers.getRandomDouble(minRandomDistance, maxRandomDistance));
                    Location end = start.clone().add(endV);
                    buildLine(start, end, action, interval);
                }
            }
        }
    }

    public static void buildRing(Location center, double rate, double tubeRate, double radius, double tubeRadius, Consumer<Location> action) {
        double pii = Math.PI * 2;
        double rateDiv = Math.PI / rate;
        double tubeDiv = Math.PI / tubeRadius;
        for (double theta = 0; theta <= pii; theta += rateDiv) {
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);
            for (double phi = 0; phi <= pii; phi += tubeDiv) {
                double finalRadius = radius + (tubeRadius * Math.cos(phi));
                double x = finalRadius * cos;
                double y = finalRadius * sin;
                double z = tubeRadius * Math.sin(phi);
                action.accept(center.clone().add(x, y, z));
            }
        }
    }

    public static void buildLightning(Location start, Vector direction, int entries, int branches, double radius, double offset, double offsetRate, double length, double lengthRate, double branch, double branchRate, Consumer<Location> action) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (entries <= 0) {
            return;
        }
        boolean inRange = true;
        while (random.nextDouble() < branch || inRange) {
            Vector randomizer = new Vector(random.nextDouble(-radius, radius), random.nextDouble(-radius, radius), random.nextDouble(-radius, radius)).normalize().multiply((random.nextDouble(-radius, radius)) * offset);
            Vector endVector = start.clone().toVector().add(direction.clone().multiply(length)).add(randomizer);
            Location end = endVector.toLocation(start.getWorld());
            if (end.distance(start) <= length) {
                inRange = true;
                continue;
            } else {
                inRange = false;
            }
            int rate = (int) (start.distance(end) / 0.1); // distance * (distance / 10)
            Vector rateDir = endVector.clone().subtract(start.toVector()).normalize().multiply(0.1);
            for (int i = 0; i < rate; i++) {
                Location loc = start.clone().add(rateDir.clone().multiply(i));
                action.accept(loc);
            }
            buildLightning(end.clone(), direction, entries - 1, branches - 1, radius, offset * offsetRate, offsetRate, length * lengthRate, lengthRate, branch * branchRate, branchRate, action);
            if (branches <= 0) {
                break;
            }
        }
    }

    public static void buildDNA(Location start, double radius, double rate, double extension, int height, int hydrogenBondDist, Consumer<Location> action1, Consumer<Location> action2) {
        int nucleotideDist = 0;
        for (double y = 0; y <= height; y += rate) {
            nucleotideDist++;
            double x = radius * Math.cos(extension * y);
            double z = radius * Math.sin(extension * y);
            Location nucleotide1 = start.clone().add(x, y, z);
            action1.accept(start.clone().add(x, y, z));
            Location nucleotide2 = start.clone().subtract(x, -y, z);
            action1.accept(start.clone().add(-x, y, -z));
            if (nucleotideDist >= hydrogenBondDist) {
                nucleotideDist = 0;
                buildLine(nucleotide1, nucleotide2, action2, rate * 2);
            }
        }
    }

    public static void buildRectangle(Location start, Location end, double rate, Consumer<Location> action) {
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());
        double maxY = Math.max(start.getY(), end.getY());
        double minY = Math.min(start.getY(), end.getY());
        for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
                action.accept(start.clone().add(x - minX, y - minY, 0));
            }
        }
    }

    public static void buildCage(Location start, Location end, double rate, double barRate, Consumer<Location> action) {
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());
        double maxZ = Math.max(start.getZ(), end.getZ());
        double minZ = Math.min(start.getZ(), end.getZ());
        double barChance = 0;
        for (double x = minX; x <= maxX; x += rate) {
            for (double z = minZ; z <= maxZ; z += rate) {
                Location barStart = start.clone().add(x - minX, 0, z - minZ);
                Location barEnd = start.clone().add(x - minX, 3, z - minZ);
                if ((x == minX || x + rate > maxX) || (z == minZ || z + rate > maxZ)) {
                    barChance++;
                    if (barChance >= barRate) {
                        barChance = 0;
                        buildLine(barStart, barEnd, action, rate);
                    }
                }
            }
        }
    }

    public static void buildCube(Location start, Location end, double rate, Consumer<Location> action) {
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());
        double maxY = Math.max(start.getY(), end.getY());
        double minY = Math.min(start.getY(), end.getY());
        double maxZ = Math.max(start.getZ(), end.getZ());
        double minZ = Math.min(start.getZ(), end.getZ());
        for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
                for (double z = minZ; z <= maxZ; z += rate) {
                    if ((y == minY || y + rate > maxY) || (x == minX || x + rate > maxX) || (z == minZ || z + rate > maxZ)) {
                        action.accept(start.clone().add(x - minX, y - minY, z - minZ));
                    }
                }
            }
        }
    }

    public static void buildCubeFilled(Location start, Location end, double rate, Consumer<Location> action) {
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());
        double maxY = Math.max(start.getY(), end.getY());
        double minY = Math.min(start.getY(), end.getY());
        double maxZ = Math.max(start.getZ(), end.getZ());
        double minZ = Math.min(start.getZ(), end.getZ());
        for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
                for (double z = minZ; z <= maxZ; z += rate) {
                    action.accept(start.clone().add(x - minX, y - minY, z - minZ));
                }
            }
        }
    }

    public static void buildCubeStructured(Location start, Location end, double rate, Consumer<Location> action) {
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());
        double maxY = Math.max(start.getY(), end.getY());
        double minY = Math.min(start.getY(), end.getY());
        double maxZ = Math.max(start.getZ(), end.getZ());
        double minZ = Math.min(start.getZ(), end.getZ());
        for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
                for (double z = minZ; z <= maxZ; z += rate) {
                    int components = 0;
                    if (x == minX || x + rate > maxX) {
                        components++;
                    }
                    if (y == minY || y + rate > maxY) {
                        components++;
                    }
                    if (z == minZ || z + rate > maxZ) {
                        components++;
                    }
                    if (components >= 2) {
                        action.accept(start.clone().add(x - minX, y - minY, z - minZ));
                    }
                }
            }
        }
    }

    public static void buildHypercube(Location startOrigin, Location endOrigin, double rate, double sizeRate, int cubes, Consumer<Location> action) {
        List<Location> previousPoints = null;
        for (int i = 0; i < cubes + 1; i++) {
            List<Location> points = new ArrayList<>();
            Location start = startOrigin.clone().subtract(i * sizeRate, i * sizeRate, i * sizeRate);
            Location end = endOrigin.clone().add(i * sizeRate, i * sizeRate, i * sizeRate);
            double maxX = Math.max(start.getX(), end.getX());
            double minX = Math.min(start.getX(), end.getX());
            double maxY = Math.max(start.getY(), end.getY());
            double minY = Math.min(start.getY(), end.getY());
            double maxZ = Math.max(start.getZ(), end.getZ());
            double minZ = Math.min(start.getZ(), end.getZ());
            points.add(new Location(start.getWorld(), maxX, maxY, maxZ));
            points.add(new Location(start.getWorld(), minX, minY, minZ));
            points.add(new Location(start.getWorld(), maxX, minY, maxZ));
            points.add(new Location(start.getWorld(), minX, maxY, minZ));
            points.add(new Location(start.getWorld(), minX, minY, maxZ));
            points.add(new Location(start.getWorld(), maxX, minY, minZ));
            points.add(new Location(start.getWorld(), maxX, maxY, minZ));
            points.add(new Location(start.getWorld(), minX, maxY, maxZ));
            if (previousPoints != null) {
                for (int p = 0; p < 8; p++) {
                    Location current = points.get(p);
                    Location previous = previousPoints.get(p);
                    buildLine(previous, current, action, rate);
                }
            }
            previousPoints = points;
            for (double x = minX; x <= maxX; x += rate) {
                for (double y = minY; y <= maxY; y += rate) {
                    for (double z = minZ; z <= maxZ; z += rate) {
                        int components = 0;
                        if (x == minX || x + rate > maxX) {
                            components++;
                        }
                        if (y == minY || y + rate > maxY) {
                            components++;
                        }
                        if (z == minZ || z + rate > maxZ) {
                            components++;
                        }
                        if (components >= 2) {
                            action.accept(start.clone().add(x - minX, y - minY, z - minZ));
                        }
                    }
                }
            }
        }
    }


    Effects() {
    }

    Effects(Particle particle, Location center) {
        this.particle = particle;
        this.center = center;
    }

    public void play() {
        if (data instanceof ColorData) {
            data = ((ColorData) data).instance();
        }
        if (player.size() > 0) {
            player.forEach(p -> p.spawnParticle(particle, Optional.ofNullable(center).orElse(p.getLocation()), count, offset[0], offset[1], offset[2], speed, data));
        }
        if (range > 0 && center != null) {
            center.getWorld().getPlayers().stream().filter(p -> p.getLocation().distance(center) < range).forEach(p -> p.spawnParticle(particle, center, count, offset[0], offset[1], offset[2], speed, data));
        }
    }

    public void playAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), this::play);
    }

    public Effects particle(Particle particle) {
        this.particle = particle;
        return this;
    }

    public Effects center(Location center) {
        this.center = center;
        return this;
    }

    public Effects offset(double[] offset) {
        this.offset = offset;
        return this;
    }

    public Effects speed(double speed) {
        this.speed = speed;
        return this;
    }

    public Effects range(double range) {
        this.range = range;
        return this;
    }

    public Effects count(int count) {
        this.count = count;
        return this;
    }

    public Effects player(List<Player> player) {
        this.player = player;
        return this;
    }

    public Effects player(Player... player) {
        this.player = ArrayUtil.asList(player);
        return this;
    }

    public Effects data(ItemStack data) {
        this.data = data;
        return this;
    }

    public Effects data(MaterialData data) {
        this.data = data;
        return this;
    }

    public Effects data(BlockData data) {
        this.data = data;
        return this;
    }

    public Effects data(ColorData data) {
        this.data = data;
        return this;
    }

    public Effects data0(Object data) {
        this.data = data;
        return this;
    }

    public static class ColorData {

        private final Color color;
        private final float size;

        public ColorData(Color color, float size) {
            Preconditions.checkArgument(color != null, "color");
            this.color = color;
            this.size = size;
        }

        public Color getColor() {
            return this.color;
        }

        public float getSize() {
            return this.size;
        }

        public Object instance() {
            try {
                return Reflection.instantiateObject(Class.forName("org.bukkit.Particle$DustOptions"), color, size);
            } catch (Throwable ignored) {
            }
            return null;
        }
    }
}
