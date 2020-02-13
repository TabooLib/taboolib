package io.izzel.taboolib.util.lite;

import com.google.common.collect.Lists;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.TMap;
import io.izzel.taboolib.util.item.Items;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @Author sky
 * @Since 2019-10-06 1:02
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

    public static Effects create(Particle particle, Location center) {
        return new Effects(particle, center);
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
                    }
                    break;
            }
        }
        return effects;
    }

    public static Particle parseParticle(String in) {
        try {
            return Particle.valueOf(in.toUpperCase());
        } catch (Throwable ignored) {
        }
        return Particle.FLAME;
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

    public static void buildPolygon(Location center, double range, double interval, Consumer<Location> action) {
        for (double i = 0; i < 360; i += (360 / interval)) {
            double radians = Math.toRadians(i);
            double cos = Math.cos(radians) * range;
            double sin = Math.sin(radians) * range;
            action.accept(center.clone().add(cos, 0, sin));
        }
    }

    Effects() {
    }

    Effects(Particle particle, Location center) {
        this.particle = particle;
        this.center = center;
    }

    public void play() {
        if (player.size() > 0) {
            player.forEach(p -> p.spawnParticle(particle, Optional.ofNullable(center).orElse(p.getLocation()), count, offset[0], offset[1], offset[2], speed, data));
        }
        if (range > 0 && center != null) {
            center.getWorld().getPlayers().stream().filter(p -> p.getLocation().distance(center) < range).forEach(p -> p.spawnParticle(particle, center, count, offset[0], offset[1], offset[2], speed, data));
        }
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

}
