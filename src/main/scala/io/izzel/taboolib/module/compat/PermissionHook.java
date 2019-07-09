package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import org.bukkit.entity.Player;

public class PermissionHook {

    public static void add(Player player, String perm) {
        InternalPluginBridge.handle().permissionAdd(player, perm);
    }

    public static void remove(Player player, String perm) {
        InternalPluginBridge.handle().permissionAdd(player, perm);
    }

    public static boolean has(Player player, String perm) {
        return InternalPluginBridge.handle().permissionHas(player, perm);
    }

    public static boolean exists() {
        return InternalPluginBridge.handle().permissionHooked();
    }
}
