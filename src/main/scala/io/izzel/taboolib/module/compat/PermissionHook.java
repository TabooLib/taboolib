package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.module.inject.TFunction;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;

@TFunction(enable = "init")
public class PermissionHook {

    private static Permission perms;

    static void init() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
    }

    public static Permission getPermission() {
        return perms;
    }

    public static void addPermission(Player player, String perm) {
        perms.playerAdd(player, perm);
    }

    public static void removePermission(Player player, String perm) {
        perms.playerRemove(player, perm);
    }

    public static boolean hasPermission(Player player, String perm) {
        return perms.playerHas(player, perm) || Arrays.stream(perms.getPlayerGroups(player)).anyMatch(group -> perms.groupHas(player.getWorld(), group, perm));
    }
}
