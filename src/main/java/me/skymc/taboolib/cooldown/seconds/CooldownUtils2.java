package me.skymc.taboolib.cooldown.seconds;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;

public class CooldownUtils2 implements Listener {
	
	private static ConcurrentHashMap<String, CooldownPack2> packlist = new ConcurrentHashMap<>();
	
	public static ConcurrentHashMap<String, CooldownPack2> getCooldownPacks() {
		return packlist;
	}

	public static void register(CooldownPack2 pack) {
		packlist.put(pack.getPackName(), pack);
		TLocale.Logger.info("COOLDOWNPACK.PACK-REGISTER-ANONYMOUS", pack.getPackName(), String.valueOf(pack.getPackSeconds()));
	}
	
	public static void register(CooldownPack2 pack, Plugin plugin) {
		pack.setPlugin(plugin.getName());
		packlist.put(pack.getPackName(), pack);
        TLocale.Logger.info("COOLDOWNPACK.PACK-REGISTER", pack.getPackName(), String.valueOf(pack.getPackSeconds()), plugin.getName());
	}
	
	public static void unregister(String name) {
		packlist.remove(name);
        TLocale.Logger.info("COOLDOWNPACK.PACK-UNREGISTER", name);
	}

    private static void unregister(CooldownPack2 pack) {
        packlist.remove(pack.getPackName());
        TLocale.Logger.info("COOLDOWNPACK.PACK-UNREGISTER-AUTO", pack.getPackName());
    }

    @EventHandler
	public void quit(PlayerQuitEvent e) {
        packlist.values().stream().filter(pack -> !pack.isCooldown(e.getPlayer().getName(), 0)).forEach(pack -> pack.unRegister(e.getPlayer().getName()));
	}
	
	@EventHandler
	public void disable(PluginDisableEvent e) {
        packlist.values().stream().filter(pack -> pack.getPlugin().equals(e.getPlugin().getName())).forEach(CooldownUtils2::unregister);
    }
}
