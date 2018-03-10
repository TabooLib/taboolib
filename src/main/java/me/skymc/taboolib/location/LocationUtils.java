package me.skymc.taboolib.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import me.skymc.taboolib.methods.MethodsUtils;

public class LocationUtils {
	
	/**
	 * 序列化
	 * 
	 * @param location
	 * @return
	 */
	public static String fromString(Location location) {
		return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
	}
	
	/**
	 * 反序列化
	 * 
	 * @param string
	 * @return
	 */
	public static Location toString(String string) {
		Location location = new Location(null, 0, 0, 0);
		try {
			location.setWorld(Bukkit.getWorld(string.split(",")[0]));
			location.setX(Double.valueOf(string.split(",")[1]));
			location.setY(Double.valueOf(string.split(",")[2]));
			location.setZ(Double.valueOf(string.split(",")[3]));
			location.setYaw(Float.valueOf(string.split(",")[4]));
			location.setPitch(Float.valueOf(string.split(",")[5]));
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		return location;
	}
	
	public static double getBetween(Location l1, Location l2) {
		if (l1.getWorld().equals(l2.getWorld())) {
			return Math.ceil(l1.distance(l2));
		}
        return -1D;
	}
	
	@Deprecated
	public static Block findBlockByLocation(Location l) {
        while(l.getY() < 255 && l.getBlock().getType() != Material.AIR) {
            l.add(0, 1, 0);
        }
        return l.getY() < 255 && l.getBlock().getType() == Material.AIR ? l.getBlock() : null;
    }
	
	@Deprecated
	public static String formatToString(Location l) {
		return l.getWorld().getName() + "," + String.valueOf(l.getX()).replace(".", "#") + "," + String.valueOf(l.getY()).replace(".", "#") + "," + String.valueOf(l.getZ()).replace(".", "#");
	}

	@Deprecated
	public static Location parsedToLocation(String string) {
		String[] values = string.split(",");
		return new Location(Bukkit.getWorld(values[0]), Double.valueOf(values[1].replace("#", ".")), Double.valueOf(values[2].replace("#", ".")), Double.valueOf(values[3].replace("#", ".")));
	}
}
