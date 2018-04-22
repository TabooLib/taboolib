package me.skymc.taboolib;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.skymc.taboolib.playerdata.DataUtils;
import net.md_5.bungee.api.ChatColor;

public class TabooLib {
	
	public static void debug(Plugin plugin, String... args) {
		if (Main.getInst().getConfig().getBoolean("DEBUG")) {
			for (String var : args) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[TabooLib - DEBUG][" + plugin.getName() + "] " + ChatColor.RED + var);
			}
		}
	}
	
	public static double getPluginVersion() {
		try {
			return Double.valueOf(Main.getInst().getDescription().getVersion());
		} catch (Exception e) {
			return 0D;
		}
	}
	
	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
	}
	
	public static String getServerUID() {
		if (!DataUtils.getPluginData("TabooLibrary", null).contains("serverUID")) {
			DataUtils.getPluginData("TabooLibrary", null).set("serverUID", UUID.randomUUID().toString());
		}
		return DataUtils.getPluginData("TabooLibrary", null).getString("serverUID");
	}
	
	public static void resetServerUID() {
		DataUtils.getPluginData("TabooLibrary", null).set("serverUID", UUID.randomUUID().toString());
	}
	
	public static int getVerint() {
		if (getVersion().startsWith("v1_7")) {
			return 10700;
		} else if (getVersion().startsWith("v1_8")) {
			return 10800;
		} else if (getVersion().startsWith("v1_9")) {
			return 10900;
		} else if (getVersion().startsWith("v1_10")) {
			return 11000;
		} else if (getVersion().startsWith("v1_11")) {
			return 11100;
		} else if (getVersion().startsWith("v1_12")) {
			return 11200;
		} else if (getVersion().startsWith("v1_13")) {
			return 11300;
		}
		return 0;
	}
}
