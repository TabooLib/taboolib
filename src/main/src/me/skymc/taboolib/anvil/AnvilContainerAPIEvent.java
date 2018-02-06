package me.skymc.taboolib.anvil;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AnvilContainerAPIEvent extends Event{
	
	public static final HandlerList handlers = new HandlerList();
	public InventoryClickEvent event;
	public String string;
	public String type;
	
	public AnvilContainerAPIEvent(InventoryClickEvent e, String t, String s)
	{
		event = e;
		string = s;
		type = t;
	}

	@Override
	public HandlerList getHandlers() 
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList() 
	{
		return handlers;
	}
}
