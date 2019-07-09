package io.izzel.taboolib.common.plugin;

import com.sk89q.worldguard.protection.managers.RegionManager;
import io.izzel.taboolib.module.lite.SimpleVersionControl;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-07-09 17:10
 */
public abstract class InternalPluginBridge {

    private static InternalPluginBridge handle;

    public static InternalPluginBridge handle() {
        return handle;
    }

    static {
        try {
            handle = (InternalPluginBridge) SimpleVersionControl.createNMS("io.izzel.taboolib.common.plugin.bridge.BridgeImpl").translateBridge().newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    abstract public <T> T getRegisteredService(Class<? extends T> clazz);

    abstract public String setPlaceholders(Player player, String args);

    abstract public List<String> setPlaceholders(Player player, List<String> args);

    abstract public void economyCreate(OfflinePlayer p);

    abstract public void economyTake(OfflinePlayer p, double d);

    abstract public void economyGive(OfflinePlayer p, double d);

    abstract public double economyLook(OfflinePlayer p);

    abstract public void permissionAdd(Player player, String perm);

    abstract public void permissionRemove(Player player, String perm);

    abstract public boolean permissionHas(Player player, String perm);

    abstract public RegionManager worldguardRegionManager(World world);

    abstract public Collection<String> worldguardGetRegions(World world);

    abstract public List<String> worldguardGetRegion(World world, Location location);

    abstract public boolean economyHooked();

    abstract public boolean permissionHooked();

    abstract public boolean placeholderHooked();

    abstract public boolean worldguardHooked();
}
