package me.skymc.taboolib.events.itag;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * @author md_5
 */
public class AsyncPlayerReceiveNameTagEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Player namedPlayer;
    private String tag;
    private UUID UUID;
    private boolean tagModified;
    private boolean UUIDModified;

    public Player getPlayer() {
        return player;
    }

    public Player getNamedPlayer() {
        return namedPlayer;
    }

    public String getTag() {
        return tag;
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public boolean isTagModified() {
        return tagModified;
    }

    public boolean isUUIDModified() {
        return UUIDModified;
    }

    public AsyncPlayerReceiveNameTagEvent(Player who, Player namedPlayer, String initialName, UUID uuid) {
        Preconditions.checkNotNull(who, "who");
        Preconditions.checkNotNull(namedPlayer, "namedPlayer");
        Preconditions.checkNotNull(initialName, "initialName");
        Preconditions.checkNotNull(uuid, "uuid");

        this.player = who;
        this.namedPlayer = namedPlayer;
        this.tag = initialName;
        this.tagModified = namedPlayer.getName().equals(initialName);
        this.UUID = uuid;
    }

    public boolean setTag(String tag) {
        Preconditions.checkNotNull(tag, "tag");

        this.tag = tag;
        this.tagModified = true;

        return tag.length() < 16;
    }

    public void setUUID(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");

        this.UUID = uuid;
        this.UUIDModified = true;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
