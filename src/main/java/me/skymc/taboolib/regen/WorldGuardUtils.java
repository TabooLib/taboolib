package me.skymc.taboolib.regen;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

@Deprecated
public class WorldGuardUtils {
	
	public static String getRegen(Location loc) {
		int x = (int) loc.getX();
		int y = (int) loc.getY() + 1;
		int z = (int) loc.getZ();
		
		Map<String, ProtectedRegion> regens = WorldGuardPlugin.inst().getRegionManager(loc.getWorld()).getRegions();
		Iterator<String> i = regens.keySet().iterator();
		while (i.hasNext())
		{
			ProtectedRegion regen = regens.get(i.next());
			if (regen.contains(x, y, z))
			{
				return regen.getId();
			}
		}
		return null;
	}
}
