package me.skymc.taboolib.cooldown.seconds;

import java.util.HashMap;

public class CooldownPack2 {
	
	private String plugin;
	private String name;
	private int seconds;
	
	private HashMap<String, Long> data = new HashMap<>();
	
	public CooldownPack2(String n, int s) {
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
	
	public int getCooldown(String player, int cutseconds) {
		if (!data.containsKey(player)) {
			return 0;
		}
		int difference = (int) ((System.currentTimeMillis() + cutseconds) - data.get(player));
		return difference >= seconds ? 0 : seconds - difference;
	}
	
	public boolean isCooldown(String player, int cutseconds) {
		if (!data.containsKey(player)) {
			data.put(player, System.currentTimeMillis());
			return false;
		}
		if (getCooldown(player, cutseconds) <= 0) {
			data.put(player, System.currentTimeMillis());
			return false;
		}
		return true;
	}
}
