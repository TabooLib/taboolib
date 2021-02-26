package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventCancellable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerAttackEvent extends EventCancellable<PlayerAttackEvent> {

    private final Player player;
    private final Entity target;

    public PlayerAttackEvent(Player player, Entity target) {
        this.player = player;
        this.target = target;
        async(!Bukkit.isPrimaryThread());
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getTarget() {
        return target;
    }
}
