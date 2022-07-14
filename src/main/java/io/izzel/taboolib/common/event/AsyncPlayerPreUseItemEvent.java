package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventCancellable;
import io.izzel.taboolib.module.nms.impl.Position;
import io.izzel.taboolib.util.item.Equipments;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * @author sky
 * @since 2021/04/29
 */
public class AsyncPlayerPreUseItemEvent extends EventCancellable<AsyncPlayerPreUseItemEvent> {

    private final Player player;
    private final Position position;
    private final Equipments hand;
    private final BlockFace direction;

    public AsyncPlayerPreUseItemEvent(Player player, Position position, Equipments hand, BlockFace direction) {
        super(true);
        this.player = player;
        this.position = position;
        this.hand = hand;
        this.direction = direction;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Position getPosition() {
        return position;
    }

    public Equipments getHand() {
        return hand;
    }

    public BlockFace getDirection() {
        return direction;
    }
}
