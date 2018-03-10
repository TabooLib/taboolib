package me.skymc.taboolib.support;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.plugin.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.regions.*;
import java.util.*;
import org.bukkit.entity.*;
import org.bukkit.*;

public class SupportWorldGuard
{
	String Source_code_from_AgarthaLib;
    private WorldGuardPlugin worldGuard;
    
    public SupportWorldGuard() {
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin != null) {
            this.worldGuard = (WorldGuardPlugin)plugin;
        }
    }
    
    public final WorldGuardPlugin getWorldGuard() {
        return this.worldGuard;
    }
    
    public final RegionManager getRegionManager(final World world) {
        return this.worldGuard.getRegionManager(world);
    }
    
    public final boolean isRegionManagerExists(final World world) {
        return this.getRegionManager(world) != null;
    }
    
    public final Collection<String> getRegionIDs(final World world) {
        final RegionManager regionManager = this.getRegionManager(world);
        return (Collection<String>)regionManager.getRegions().keySet();
    }
    
    public final Collection<ProtectedRegion> getRegions(final World world) {
        final RegionManager regionManager = this.getRegionManager(world);
        return regionManager.getRegions().values();
    }
    
    public final ProtectedRegion getRegion(final World world, final String id) {
        final RegionManager regionManager = this.getRegionManager(world);
        if (regionManager != null) {
            for (final String key : regionManager.getRegions().keySet()) {
                if (key.equalsIgnoreCase(id)) {
                    return regionManager.getRegion(key);
                }
            }
        }
        return null;
    }
    
    public final boolean isRegionExists(final World world, final String id) {
        return this.getRegion(world, id) != null;
    }
    
    public final boolean isPlayerInsideRegion(final ProtectedRegion region, final Player player) {
        final Location location = player.getLocation();
        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
