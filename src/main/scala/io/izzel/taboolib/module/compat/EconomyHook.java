package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.module.inject.TFunction;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @Author 坏黑
 * @Since 2019-07-05 18:50
 */
@TFunction(enable = "init")
public class EconomyHook {

    private static Economy economy;

    static void init() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> l = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (l != null) {
            economy = l.getProvider();
        }
    }

    public static void remove(OfflinePlayer p, double d) {
        economy.withdrawPlayer(p, d);
    }

    public static void add(OfflinePlayer p, double d) {
        economy.depositPlayer(p, d);
    }

    public static void set(OfflinePlayer p, double d) {
        add(p, d - get(p));
    }

    public static double get(OfflinePlayer p) {
        return economy.getBalance(p);
    }

    public static void create(OfflinePlayer p) {
        economy.createPlayerAccount(p);
    }

    public static boolean exists() {
        return economy != null;
    }

    public static net.milkbowl.vault.economy.Economy getEconomy() {
        return economy;
    }
}
