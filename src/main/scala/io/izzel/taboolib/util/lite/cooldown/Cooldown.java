package io.izzel.taboolib.util.lite.cooldown;

import java.util.HashMap;

public class Cooldown {

	private String plugin;
	private String name;
	private long seconds;

	private HashMap<String, Long> data = new HashMap<>();

	public Cooldown(String n, int s) {
		this.name = n;
		this.seconds = s;
		this.plugin = "null";
	}

	public Cooldown(String n, long s) {
		this.name = n;
		this.seconds = s;
		this.plugin = "null";
	}

	public String getPackName() {
		return name;
	}

	public long getPackSeconds() {
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

	public long getCooldown(String player) {
		return getCooldown(player, 0);
	}

	public long getCooldown(String player, long cutSeconds) {
		if (!data.containsKey(player)) {
			return 0;
		}
		long difference = ((System.currentTimeMillis() + cutSeconds) - data.get(player));
		return difference >= seconds ? 0 : seconds - difference;
	}

	public boolean isCooldown(String player) {
		return isCooldown(player, 0L);
	}

	public boolean isCooldown(String player, long cutSeconds) {
		if (!data.containsKey(player)) {
			data.put(player, System.currentTimeMillis());
			return false;
		}
		if (getCooldown(player, cutSeconds) <= 0) {
			data.put(player, System.currentTimeMillis());
			return false;
		}
		return true;
	}

	public void reset(String player) {
		data.remove(player);
	}
}
