package me.skymc.taboolib.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.database.PlayerDataManager;
import me.skymc.taboolib.playerdata.DataUtils;

public class ListenerPlayerQuit implements Listener{
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void quit(PlayerQuitEvent e) {
		DataUtils.saveOnline(e.getPlayer().getName());
	}
}
