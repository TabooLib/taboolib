package io.izzel.taboolib.module.event;

import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Field;

/**
 * @Author sky
 * @Since 2019-10-22 10:25
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class EventNormal<T extends EventNormal> extends Event {

    protected static final HandlerList handlers = new HandlerList();

    protected static HandlerList getHandlerList() {
        return handlers;
    }

    public T call() {
        try {
            Bukkit.getPluginManager().callEvent(this);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return (T) this;
    }

    public T async(boolean value) {
        try {
            Field asyncField = Reflection.getField(Event.class, true, "async");
            Ref.putField(this, asyncField, value);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return (T) this;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
