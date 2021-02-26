package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventNormal;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerKeepAliveEvent extends EventNormal<PlayerKeepAliveEvent> {

    private final Player player;
    private final boolean isFirst;

    public PlayerKeepAliveEvent(Player player, boolean isFirst) {
        this.player = player;
        this.isFirst = isFirst;
        async(!Bukkit.isPrimaryThread());
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isFirst() {
        return isFirst;
    }
}
