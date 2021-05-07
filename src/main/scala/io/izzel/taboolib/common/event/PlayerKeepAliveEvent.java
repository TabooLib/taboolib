package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventNormal;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 玩家心跳包事件
 *
 * @author sky
 * @since 2021/04/29
 */
public class PlayerKeepAliveEvent extends EventNormal<PlayerKeepAliveEvent> {

    private final Player player;
    private final boolean isFirst;

    public PlayerKeepAliveEvent(Player player, boolean isFirst) {
        super(true);
        this.player = player;
        this.isFirst = isFirst;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isFirst() {
        return isFirst;
    }
}
