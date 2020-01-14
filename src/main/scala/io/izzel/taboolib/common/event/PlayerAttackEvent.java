package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventCancellable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerAttackEvent extends EventCancellable<PlayerAttackEvent> {

    private Player player;
    private Entity target;

    public PlayerAttackEvent(Player player, Entity target) {
        this.player = player;
        this.target = target;
        async(true);
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getTarget() {
        return target;
    }
}
