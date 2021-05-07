package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventNormal;
import org.bukkit.entity.Player;

public class PlayerHologramDisplayEvent extends EventNormal<PlayerHologramDisplayEvent> {

    private final Player player;
    private String text;

    public PlayerHologramDisplayEvent(Player player, String text) {
        super(true);
        this.player = player;
        this.text = text;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
