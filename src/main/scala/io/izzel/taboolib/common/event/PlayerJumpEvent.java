package io.izzel.taboolib.common.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerJumpEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;
    private Player player;

    public PlayerJumpEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerJumpEvent call() {
        Bukkit.getPluginManager().callEvent(this);
        return this;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean e) {
        this.isCancelled = e;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
