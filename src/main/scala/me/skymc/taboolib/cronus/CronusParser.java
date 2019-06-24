package me.skymc.taboolib.cronus;

import me.skymc.taboolib.cronus.bukkit.ItemStack;
import me.skymc.taboolib.cronus.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.util.NumberConversions;

import java.util.Arrays;

/**
 * @Author 坏黑
 * @Since 2019-05-23 22:43
 */
public class CronusParser {

    public static Location toLocation(Object in) {
        String str = String.valueOf(in);
        // 区域
        // world:0,80,0~0,90,0
        if (str.contains(":") && str.contains("~")) {
            String[] area = str.split("~");
            try {
                return new Location(Location.Mode.AREA, new org.bukkit.Location[] {toBukkitLocation(area[0].replace(":", ",")), toBukkitLocation(area[0].split(":")[0] + "," + area[1])}, null);
            } catch (Throwable ignored) {
                return new Location(Location.Mode.AREA, null, null);
            }
        }
        // 范围
        // world:0,80,0 r:10
        else if (str.contains("r:")) {
            String[] range = str.split("r:");
            return new Location(Location.Mode.RANGE, new org.bukkit.Location[] {toBukkitLocation(range[0].replace(":", ","))}, NumberConversions.toInt(range[1]));
        }
        // 单项
        // world,0,80,0;world,0,90,0
        else {
            return new Location(Location.Mode.POINT, null, Arrays.stream(str.split(";")).map(CronusParser::toBukkitLocation).toArray(org.bukkit.Location[]::new));
        }
    }

    public static ItemStack toItemStack(Object in) {
        String type = null;
        String name = null;
        String lore = null;
        int damage = -1;
        int amount = 1;
        for (String v : String.valueOf(in).split(",")) {
            if (v.toLowerCase().startsWith("type=")) {
                type = v.substring("type=".length());
            } else if (v.toLowerCase().startsWith("t=")) {
                type = v.substring("t=".length());
            } else if (v.toLowerCase().startsWith("name=")) {
                name = v.substring("name=".length());
            } else if (v.toLowerCase().startsWith("n=")) {
                name = v.substring("n=".length());
            } else if (v.toLowerCase().startsWith("lore=")) {
                lore = v.substring("lore=".length());
            } else if (v.toLowerCase().startsWith("l=")) {
                lore = v.substring("l=".length());
            } else if (v.toLowerCase().startsWith("damage=")) {
                damage = NumberConversions.toInt(v.substring("damage=".length()));
            } else if (v.toLowerCase().startsWith("d=")) {
                damage = NumberConversions.toInt(v.substring("d=".length()));
            } else if (v.toLowerCase().startsWith("amount=")) {
                amount = NumberConversions.toInt(v.substring("amount=".length()));
            } else if (v.toLowerCase().startsWith("a=")) {
                amount = NumberConversions.toInt(v.substring("a=".length()));
            } else {
                type = v;
            }
        }
        return new ItemStack(type, name, lore, damage, amount);
    }

    public static org.bukkit.Location toBukkitLocation(Object in) {
        String[] v = String.valueOf(in).split(",");
        return new org.bukkit.Location(
                v.length > 0 ? Bukkit.getWorld(v[0]) : Bukkit.getWorlds().iterator().next(),
                v.length > 1 ? NumberConversions.toDouble(v[1]) : 0,
                v.length > 2 ? NumberConversions.toDouble(v[2]) : 0,
                v.length > 3 ? NumberConversions.toDouble(v[3]) : 0,
                v.length > 4 ? NumberConversions.toFloat(v[4]) : 0,
                v.length > 5 ? NumberConversions.toFloat(v[5]) : 0);
    }
}
