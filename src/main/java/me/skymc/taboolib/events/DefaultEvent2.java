package me.skymc.taboolib.events;

import org.bukkit.event.player.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class DefaultEvent2 extends PlayerEvent
{
    private static final HandlerList handlers;
    
    static {
        handlers = new HandlerList();
    }
    
    private DefaultEvent2(final Player who) {
        super(who);
    }
    
    public static HandlerList getHandlerList() {
        return DefaultEvent2.handlers;
    }
    
    public HandlerList getHandlers() {
        return DefaultEvent2.handlers;
    }
    
    public static class Pre extends DefaultEvent2 implements Cancellable
    {
        private boolean cancelled;
        
        public Pre(Player who) {
            super(who);
            this.cancelled = false;
        }
        
        public boolean isCancelled() {
            return this.cancelled;
        }
        
        public void setCancelled(final boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
    
    public static class Post extends DefaultEvent2
    {
        public Post(Player who) {
            super(who);
        }
    }
}
