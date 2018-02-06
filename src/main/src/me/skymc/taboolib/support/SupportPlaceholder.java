package me.skymc.taboolib.support;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import me.skymc.taboolib.database.GlobalDataManager;

public class SupportPlaceholder extends EZPlaceholderHook {

	public SupportPlaceholder(Plugin plugin, String identifier) {
		super(plugin, identifier);
	}

	@Override
	public String onPlaceholderRequest(Player player, String args) {
		if (args.startsWith("variable_")) {
			String[] value = args.split("_");
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < value.length ; i++) {
				sb.append(value[i] + "_");
			}
			return GlobalDataManager.getVariableAsynchronous(sb.substring(0, sb.length() - 1), "<none>");
		}
		return null;
	}

}
