package io.izzel.taboolib.common.event;

import io.izzel.taboolib.module.event.EventCancellable;
import io.izzel.taboolib.module.nms.impl.Position;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * @author sky
 * @since 2021/04/29
 */
public class AsyncPlayerPreBlockDigEvent extends EventCancellable<AsyncPlayerPreBlockDigEvent> {

    private final Player player;
    private final Position position;
    private final BlockFace direction;
    private final Type type;

    public AsyncPlayerPreBlockDigEvent(Player player, Position position, BlockFace direction, Type type) {
        super(true);
        this.player = player;
        this.position = position;
        this.direction = direction;
        this.type = type;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Position getPosition() {
        return position;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public Type getType() {
        return type;
    }

    public enum Type {

        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND
    }
}
