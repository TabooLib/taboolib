package me.skymc.taboolib.cooldown.seconds;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

public class CooldownUtils2 implements Listener {
	
	private static ConcurrentHashMap<String, CooldownPack2> packlist = new ConcurrentHashMap<>();
	
	public static ConcurrentHashMap<String, CooldownPack2> getCooldownPacks() {
		return packlist;
	}

	public static void register(CooldownPack2 pack) {
		packlist.put(pack.getPackName(), pack);
//		MsgUtils.send("注册冷却包: " + pack.getPackName() + ", 时间: " + pack.getPackSeconds() + " 秒 (匿名注册)");
	}
	
	public static void register(CooldownPack2 pack, Plugin plugin) {
		pack.setPlugin(plugin.getName());
		
		packlist.put(pack.getPackName(), pack);
//		MsgUtils.send("注册冷却包: " + pack.getPackName() + ", 时间: " + pack.getPackSeconds() + " 秒 (" + plugin.getName() + ")");
	}
	
	public static void unregister(String name) {
		packlist.remove(name);
		
//		MsgUtils.send("注销冷却包: " + name + " (主动注销)");
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent e) {
		for (CooldownPack2 pack : packlist.values()) {
			if (!pack.isCooldown(e.getPlayer().getName(), 0)) {
				pack.unRegister(e.getPlayer().getName());
			}
		}
	}
	
	@EventHandler
	public void disable(PluginDisableEvent e) {
		for (CooldownPack2 pack : packlist.values()) {
			if (pack.getPlugin().equals(e.getPlugin().getName())) {
				packlist.remove(pack.getPackName());
				
//				MsgUtils.send("注销冷却包: " + pack.getPackName() + " (自动注销)");
			}
		}
	}
}
