package me.skymc.taboolib.cooldown;

import java.util.HashMap;

import org.bukkit.plugin.Plugin;

@Deprecated
public class CooldownPack {
	
	private String plugin;
	private String name;
	private int seconds;
	
	private HashMap<String, Long> data = new HashMap<>();
	
	public CooldownPack(String n, int s) {
		this.name = n;
		this.seconds = s;
		this.plugin = "null";
	}
	
	public String getPackName() {
		return name;
	}
	
	public int getPackSeconds() {
		return seconds;
	}
	
	public String getPlugin() {
		return plugin;
	}
	
	public void setPlugin(String p) {
		this.plugin = p;
	}
	
	public void unRegister(String player) {
		data.remove(player);
	}
	
	public int getCooldown(String player) {
		if (!data.containsKey(player)) {
			return 0;
		}
		int difference = (int) ((System.currentTimeMillis() - data.get(player)) / 1000);
		
		return difference >= seconds ? 0 : seconds - difference;
	}
	
	public boolean isCooldown(String player, int cutseconds) {
		if (!data.containsKey(player)) {
			data.put(player, System.currentTimeMillis());
			return false;
		}
		if ((getCooldown(player) - (cutseconds*1000)) <= 0) {
			data.put(player, System.currentTimeMillis());
			return false;
		}
		return true;
	}
}
