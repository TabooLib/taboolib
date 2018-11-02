package me.skymc.taboolib.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class TPluginLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Plugin plugin;

    public TPluginLoadEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
