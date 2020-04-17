package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventCancellable;
import org.bukkit.entity.Player;

public class PlayerJumpEvent extends EventCancellable<PlayerJumpEvent> {

    private final Player player;

    public PlayerJumpEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }
}
