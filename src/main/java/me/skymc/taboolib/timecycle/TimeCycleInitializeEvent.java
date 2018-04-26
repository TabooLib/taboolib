package me.skymc.taboolib.timecycle;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TimeCycleInitializeEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private TimeCycle cycle;
	private Long time;
  
	public TimeCycleInitializeEvent(TimeCycle cycle, Long time) {
		super(true);
		this.cycle = cycle;
		this.time = time;
	}
	
	public TimeCycleInitializeEvent call() {
		Bukkit.getPluginManager().callEvent(this);
		return this;
	}
	
	public Long getTimeline() {
		return time;
	}
	
	public void setTimeLine(Long time) {
		this.time = time;
	}
  
	public TimeCycle getCycle() {
		return this.cycle;
	}
  
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
  
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	
}
