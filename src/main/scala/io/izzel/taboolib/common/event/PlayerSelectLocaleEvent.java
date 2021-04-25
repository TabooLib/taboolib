package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventNormal;
import org.bukkit.entity.Player;

public class PlayerSelectLocaleEvent extends EventNormal<PlayerSelectLocaleEvent> {

    private final Player player;
    private String locale;

    public PlayerSelectLocaleEvent(Player player, String locale) {
        super(true);
        this.player = player;
        this.locale = locale;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
