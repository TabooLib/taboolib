package me.skymc.taboolib.message;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import me.skymc.taboolib.Main;

public class MsgUtils{
	
	public static void send(CommandSender sender, String s) {
		sender.sendMessage(Main.getPrefix() + s.replaceAll("&", "¡ì"));
	}
	
	public static void send(org.bukkit.entity.Player player, String s) {
		player.sendMessage(Main.getPrefix() + s.replaceAll("&", "¡ì"));
	}
	
	public static void send(String s) {
		Bukkit.getConsoleSender().sendMessage(Main.getPrefix() + s.replaceAll("&", "¡ì"));
	}
	
	public static void warn(String s) {
		warn(s, Main.getInst());
	}
	
	public static void send(String s, Plugin plugin) {
		Bukkit.getConsoleSender().sendMessage("¡ì8[¡ì3" + plugin.getName() + "¡ì8] ¡ì7" + s.replaceAll("&", "¡ì"));
	}
	
	public static void warn(String s, Plugin plugin) {
		Bukkit.getConsoleSender().sendMessage("¡ì4[¡ìc" + plugin.getName() + "¡ì4][WARN #!] ¡ìc" + s.replaceAll("&", "¡ì"));
	}
	
	@Deprecated
	public static void Console(String s) {
		Bukkit.getConsoleSender().sendMessage(Main.getPrefix() + s.replaceAll("&", "¡ì"));
	}
	
	@Deprecated
	public static void System(String s) {
		System.out.println("[TabooLib] " + s);
	}
	
	@Deprecated
	public static void Sender(CommandSender p, String s) {
		p.sendMessage(Main.getPrefix() + s.replaceAll("&", "¡ì"));
	}
	
	@Deprecated
	public static void Player(org.bukkit.entity.Player p, String s) {
		p.sendMessage(Main.getPrefix() + s.replaceAll("&", "¡ì"));
	}
	
	@Deprecated
	public static String noPe() {
		String s = Main.getInst().getConfig().getString("NO-PERMISSION-MESSAGE").replaceAll("&", "¡ì");
		if (s.equals("")) {
			s = "¡ìcCONFIG ERROR ¡ì8(NO-PERMISSION-MESSAGE)";
		}
		return s;
	}
	
	@Deprecated
	public static String noClaim(String a) {
		String s = Main.getInst().getConfig().getString("NO-CLAIM-MESSAGE").replaceAll("&", "¡ì").replaceAll("%s%", a);
		if (s.equals("")) {
			s = "¡ìcCONFIG ERROR ¡ì8(NO-CLAIM-MESSAGE)";
		}
		return s;
	}

}
