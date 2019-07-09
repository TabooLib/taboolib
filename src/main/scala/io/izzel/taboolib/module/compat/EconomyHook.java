package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import org.bukkit.OfflinePlayer;

/**
 * @Author 坏黑
 * @Since 2019-07-05 18:50
 */
public class EconomyHook {

    public static void add(OfflinePlayer p, double d) {
        InternalPluginBridge.handle().economyGive(p, d);
    }

    public static void remove(OfflinePlayer p, double d) {
        InternalPluginBridge.handle().economyTake(p, d);
    }

    public static void set(OfflinePlayer p, double d) {
        add(p, d - get(p));
    }

    public static double get(OfflinePlayer p) {
        return InternalPluginBridge.handle().economyLook(p);
    }

    public static void create(OfflinePlayer p) {
        InternalPluginBridge.handle().economyCreate(p);
    }

    public static boolean exists() {
        return InternalPluginBridge.handle().economyHooked();
    }
}
