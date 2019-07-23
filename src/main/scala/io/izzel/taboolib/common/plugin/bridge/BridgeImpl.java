package io.izzel.taboolib.common.plugin.bridge;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import io.izzel.taboolib.util.Reflection;
import me.clip.placeholderapi.PlaceholderAPI;
import me.skymc.taboolib.database.PlayerDataManager;
import me.skymc.taboolib.sound.SoundPack;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import protocolsupport.api.ProtocolSupportAPI;
import us.myles.ViaVersion.api.Via;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BridgeImpl extends InternalPluginBridge {

    private Object economy;
    private Object permission;
    private Method getRegionManager;
    private boolean placeholder;
    private boolean worldguard;

    public BridgeImpl() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            economy = getRegisteredService(Economy.class);
            permission = getRegisteredService(Permission.class);
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            if (!WorldGuardPlugin.inst().getDescription().getVersion().startsWith("7")) {
                try {
                    getRegionManager = WorldGuardPlugin.class.getDeclaredMethod("getRegionManager", World.class);
                    getRegionManager.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            worldguard = true;
        }
        placeholder = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public <T> T getRegisteredService(Class<? extends T> clazz) {
        RegisteredServiceProvider registeredServiceProvider = Bukkit.getServer().getServicesManager().getRegistration(clazz);
        return registeredServiceProvider == null ? null : (T) registeredServiceProvider.getProvider();
    }

    @Override
    public String setPlaceholders(Player player, String args) {
        return placeholder ? PlaceholderAPI.setPlaceholders(player, args) : args;
    }

    @Override
    public List<String> setPlaceholders(Player player, List<String> args) {
        return placeholder ? PlaceholderAPI.setPlaceholders(player, args) : args;
    }

    @Override
    public void economyCreate(OfflinePlayer p) {
        if (economy instanceof Economy) {
            ((Economy) economy).createPlayerAccount(p);
        }
    }

    @Override
    public void economyTake(OfflinePlayer p, double d) {
        if (economy instanceof Economy) {
            ((Economy) economy).withdrawPlayer(p, d);
        }
    }

    @Override
    public void economyGive(OfflinePlayer p, double d) {
        if (economy instanceof Economy) {
            ((Economy) economy).depositPlayer(p, d);
        }
    }

    @Override
    public double economyLook(OfflinePlayer p) {
        return economy instanceof Economy ? ((Economy) economy).getBalance(p) : 0;
    }

    @Override
    public void permissionAdd(Player player, String perm) {
        if (permission instanceof Permission) {
            ((Permission) permission).playerAdd(player, perm);
        }
    }

    @Override
    public void permissionRemove(Player player, String perm) {
        if (permission instanceof Permission) {
            ((Permission) permission).playerRemove(player, perm);
        }
    }

    @Override
    public boolean permissionHas(Player player, String perm) {
        return permission instanceof Permission && ((Permission) permission).playerHas(player, perm);
    }

    @Override
    public Collection<String> worldguardGetRegions(World world) {
        return worldguardRegionManager(world).getRegions().keySet();
    }

    @Override
    public List<String> worldguardGetRegion(World world, Location location) {
        return worldguardRegionManager(world).getRegions().values().stream().filter(r -> r.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())).map(ProtectedRegion::getId).collect(Collectors.toList());
    }

    @Override
    public RegionManager worldguardRegionManager(World world) {
        if (WorldGuardPlugin.inst().getDescription().getVersion().startsWith("7")) {
            return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        } else {
            try {
                return (RegionManager) getRegionManager.invoke(WorldGuardPlugin.inst(), world);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean economyHooked() {
        return economy != null;
    }

    @Override
    public boolean permissionHooked() {
        return permission != null;
    }

    @Override
    public boolean placeholderHooked() {
        return placeholder;
    }

    @Override
    public boolean worldguardHooked() {
        return worldguard;
    }

    @Override
    public Map<String, Object> taboolibTLocaleSerialize(Object in) {
        switch (in.getClass().getSimpleName()) {
            case "TLocaleText": {
                Map<String, Object> map = Maps.newHashMap();
                try {
                    map.put("text", Reflection.getValue(in, true, "text"));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return map;
            }
            case "TLocaleSound": {
                Map<String, Object> map = Maps.newHashMap();
                try {
                    List<SoundPack> sounds = (List<SoundPack>) Reflection.getValue(in, true, "soundPacks");
                    map.put("sounds", sounds.stream().map(s -> s.getSound() + "-" + s.getA() + "-" + s.getB() + "-" + s.getDelay()).collect(Collectors.toList()));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return map;
            }
            case "TLocaleBossBar": {
                Map<String, Object> map = Maps.newHashMap();
                try {
                    map.put("text", Reflection.getValue(in, true, "text"));
                    map.put("color", Reflection.getValue(in, true, "color"));
                    map.put("style", Reflection.getValue(in, true, "style"));
                    map.put("progress", Reflection.getValue(in, true, "progress"));
                    map.put("timeout", Reflection.getValue(in, true, "timeout"));
                    map.put("timeoutInterval", Reflection.getValue(in, true, "timeoutInterval"));
                    map.put("papi", Reflection.getValue(in, true, "papi"));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return map;
            }
            default:
                return ((com.ilummc.tlib.resources.TLocaleSerialize) in).serialize();
        }
    }

    @Override
    public FileConfiguration taboolibGetPlayerData(String username) {
        return PlayerDataManager.getPlayerData(username, true);
    }

    @Override
    public int protocolSupportPlayerVersion(Player player) {
        return ProtocolSupportAPI.getProtocolVersion(player).getId();
    }

    @Override
    public int viaVersionPlayerVersion(Player player) {
        return Via.getAPI().getPlayerVersion(player);
    }

    @Override
    public Class getClass(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }
}