package me.skymc.taboolib.support;

import com.google.common.base.Preconditions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author AgarthaLib
 */
public class SupportWorldGuard {

    private WorldGuardPlugin worldGuard;

    public SupportWorldGuard() {
        Preconditions.checkNotNull(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"), "WorldGuard was not found.");
        worldGuard = WorldGuardPlugin.inst();
    }

    public WorldGuardPlugin getWorldGuard() {
        return this.worldGuard;
    }

    public RegionManager getRegionManager(World world) {
        return this.worldGuard.getRegionManager(world);
    }

    public boolean isRegionManagerExists(World world) {
        return this.getRegionManager(world) != null;
    }

    public Collection<String> getRegionIDs(World world) {
        return getRegionManager(world).getRegions().keySet();
    }

    public Collection<ProtectedRegion> getRegions(World world) {
        return getRegionManager(world).getRegions().values();
    }

    public List<String> getRegionsAtLocation(World world, Location location) {
        return getRegions(world).stream().filter(protectedRegion -> protectedRegion.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())).map(ProtectedRegion::getId).collect(Collectors.toList());
    }

    public ProtectedRegion getRegion(World world, String id) {
        RegionManager regionManager = this.getRegionManager(world);
        return regionManager != null ? regionManager.getRegions().keySet().stream().filter(key -> key.equalsIgnoreCase(id)).findFirst().map(regionManager::getRegion).orElse(null) : null;
    }

    public boolean isRegionExists(World world, String id) {
        return this.getRegion(world, id) != null;
    }

    public boolean isPlayerInsideRegion(ProtectedRegion region, Player player) {
        Location location = player.getLocation();
        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
