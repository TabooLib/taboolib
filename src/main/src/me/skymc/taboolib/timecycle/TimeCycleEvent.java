package me.skymc.taboolib.timecycle;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TimeCycleEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private TimeCycle cycle;
  
	public TimeCycleEvent(TimeCycle cycle) {
		super(true);
		this.cycle = cycle;
	}
  
	public TimeCycle getCycle() {
		return this.cycle;
	}
  
	public HandlerList getHandlers() {
		return handlers;
	}
  
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
