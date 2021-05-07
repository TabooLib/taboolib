package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventCancellable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * 玩家攻击事件
 *
 * @author sky
 * @since 2021/04/29
 */
public class PlayerAttackEvent extends EventCancellable<PlayerAttackEvent> {

    private final Player player;
    private final Entity target;

    public PlayerAttackEvent(Player player, Entity target) {
        super(true);
        this.player = player;
        this.target = target;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getTarget() {
        return target;
    }
}
