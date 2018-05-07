package me.skymc.taboolib.economy;

import me.skymc.taboolib.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EcoUtils {

    public static void setupEconomy() {
        RegisteredServiceProvider<Economy> l = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (l != null) {
            Main.setEconomy(l.getProvider());
        }
    }

    public static void remove(OfflinePlayer p, double d) {
        Main.getEconomy().withdrawPlayer(p, d);
    }

    public static void add(OfflinePlayer p, double d) {
        Main.getEconomy().depositPlayer(p, d);
    }

    public static double get(OfflinePlayer p) {
        return Main.getEconomy().getBalance(p);
    }

    public static void create(OfflinePlayer p) {
        Main.getEconomy().createPlayerAccount(p);
    }
}
