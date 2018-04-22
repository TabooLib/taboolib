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

@Immutable
@SerializableAs("TITLE")
@Data
public class TLocaleTitle implements TLocaleSendable, ConfigurationSerializable {
	
	private String title;
	private String subtitle;
	private int fadein;
	private int fadeout;
	private int stay;
	
	private boolean usePlaceholder;
	
	private TLocaleTitle(boolean usePlaceholder) {
		this.usePlaceholder = usePlaceholder;
	}

	@Override
	public void sendTo(CommandSender sender, String... args) {
		// TODO Auto-generated method stub
	}

	@Override
	public String asString(String... args) {
		// TODO Auto-generated method stub
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
            title = new TLocaleTitle((boolean) map.getOrDefault("papi", TLib.getTLib().getConfig().isEnablePlaceholderHookByDefault()));
            title.setTitle((String) map.getOrDefault("title", ""));
            title.setSubtitle((String) map.getOrDefault("subtitle", ""));
            title.setFadein((int) map.getOrDefault("fadein", 10));
            title.setFadeout((int) map.getOrDefault("fadeout", 10));
            title.setStay((int) map.getOrDefault("stay", 10));
		} catch (Exception e) {
			title = new TLocaleTitle(false);
			title.setTitle("§4Load failed!");
			title.setSubtitle("§c" + e.getMessage());
		}
		return title;
	}

}
