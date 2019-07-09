package io.izzel.taboolib.common.plugin.bridge;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;

public class BridgeImpl extends InternalPluginBridge {

    @Override
    public String setPlaceholders(Player player, String args) {
        return PlaceholderAPI.setPlaceholders(player, args);
    }

    @Override
    public List<String> setPlaceholders(Player player, List<String> args) {
        return PlaceholderAPI.setPlaceholders(player, args);
    }

    @Override
    public Economy getEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Economy> registration = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        return registration != null ? registration.getProvider() : null;
    }

    @Override
    public Permission getPermission() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Permission> registration = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        return registration != null ? registration.getProvider() : null;
    }

    @Override
    public WorldGuard getWorldGuard() {
        return WorldGuard.getInstance();
    }

    @Override
    public WorldGuardPlugin getWorldGuardPlugin() {
        return WorldGuardPlugin.inst();
    }
}