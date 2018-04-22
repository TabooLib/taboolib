package com.ilummc.tlib.resources.type;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.resources.TLocaleSendable;

import lombok.Data;
import lombok.Getter;
import net.minecraft.server.v1_11_R1.EntityEvoker.e;

/**
 * @author Bkm016
 * @since 2018-04-22
 */

@SerializableAs("TITLE")
public class TLocaleTitle implements TLocaleSendable, ConfigurationSerializable {
	
	private final String title;
	private final String subtitle;
	private final int fadein;
	private final int fadeout;
	private final int stay;
	
	private boolean usePlaceholder;
	
	private TLocaleTitle(String title, String subString, int fadein, int fadeout, int stay, boolean usePlaceholder) {
		this.title = title;
		this.subtitle = subString;
		this.fadein = fadein;
		this.fadeout = fadeout;
		this.stay = stay;
		this.usePlaceholder = usePlaceholder;
	}

	@Override
	public void sendTo(CommandSender sender, String... args) {
	}

	@Override
	public String asString(String... args) {
		return null;
	}
	
	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> map = Maps.newHashMap();
		map.put("papi", usePlaceholder);
		map.put("title", title);
		map.put("subtitle", subtitle);
		map.put("fadein", fadein);
		map.put("fadeout", fadeout);
		map.put("stay", stay);
		return map;
	}
	
	public static TLocaleTitle valueOf(Map<String, Object> map) {
		TLocaleTitle title;
		try {
            title = new TLocaleTitle(
            		(String) map.getOrDefault("title", ""), 
            		(String) map.getOrDefault("subtitle", ""),
            		(int) map.getOrDefault("fadein", 10),
            		(int) map.getOrDefault("fadeout", 10),
            		(int) map.getOrDefault("stay", 20),
            		(boolean) map.getOrDefault("papi", TLib.getTLib().getConfig().isEnablePlaceholderHookByDefault()));
		} catch (Exception e) {
			title = new TLocaleTitle("§4Load failed!", "§c" + e.getMessage(), 10, 20, 10, false);
		}
		return title;
	}

}
