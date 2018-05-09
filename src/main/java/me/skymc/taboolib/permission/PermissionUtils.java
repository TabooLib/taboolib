package me.skymc.taboolib.permission;

import me.skymc.taboolib.Main;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermissionUtils {
	
	private static Permission perms;
	
	public static void loadRegisteredServiceProvider() {
		RegisteredServiceProvider<Permission> rsp = Main.getInst().getServer().getServicesManager().getRegistration(Permission.class);
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
		if (perms.playerHas(player, perm)) {
			return true;
		}
		for (String group : perms.getPlayerGroups(player)) {
			if (perms.groupHas(player.getWorld(), group, perm)) {
				return true;
			}
		}
		return false;
	}
}
