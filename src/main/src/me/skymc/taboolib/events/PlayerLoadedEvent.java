package me.skymc.taboolib.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLoadedEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private Player player;
  
	public PlayerLoadedEvent(Player player) {
		this.player = player;
	}
  
	public Player getPlayer() {
		return this.player;
	}
  
	public HandlerList getHandlers() {
		return handlers;
	}
  
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
