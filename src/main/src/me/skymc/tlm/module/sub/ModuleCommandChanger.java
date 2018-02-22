package me.skymc.tlm.module.sub;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.skymc.tlm.module.ITabooLibraryModule;

/**
 * @author sky
 * @since 2018Äê2ÔÂ22ÈÕ ÏÂÎç1:32:29
 */
public class ModuleCommandChanger implements ITabooLibraryModule, Listener {

	@Override
	public String getName() {
		return "CommandChanger";
	}
	
	@EventHandler
	public void command(PlayerCommandPreprocessEvent e) {
		// Ñ­»·ÃüÁî
		for (String id : getConfig().getConfigurationSection("Commands").getKeys(false)) {
			// »ñÈ¡ÃüÁî
			String key = getConfig().getString("Commands." + id + ".Input");
			// ÅĞ¶ÏÃüÁî
			if (e.getMessage().startsWith(key)) {
				// ÅĞ¶ÏÖ´ĞĞ·½Ê½
				if (!getConfig().contains("Commands." + id + ".ReplaceMode") || getConfig().getString("Commands." + id + ".ReplaceMode").equals("PLAYER")) {
					// Ìæ»»ÃüÁî
					e.setMessage(e.getMessage().replace(key, getConfig().getString("Commands." + id + ".Replace")));
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void command(ServerCommandEvent e) {
		// Ñ­»·ÃüÁî
		for (String id : getConfig().getConfigurationSection("Commands").getKeys(false)) {
			// »ñÈ¡ÃüÁî
			String key = getConfig().getString("Commands." + id + ".Input");
			// ÅĞ¶ÏÃüÁî
			if (e.getCommand().startsWith(key)) {
				// ÅĞ¶ÏÖ´ĞĞ·½Ê½
				if (!getConfig().contains("Commands." + id + ".ReplaceMode") || getConfig().getString("Commands." + id + ".ReplaceMode").equals("CONSOLE")) {
					// Ìæ»»ÃüÁî
					e.setCommand(e.getCommand().replace(key, getConfig().getString("Commands." + id + ".Replace")));
					return;
				}
			}
		}
	}
}
