package io.izzel.taboolib.common.listener;

import com.google.common.collect.Lists;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.common.event.AsyncPlayerPreBlockDigEvent;
import io.izzel.taboolib.common.event.AsyncPlayerPreUseItemEvent;
import io.izzel.taboolib.common.event.PlayerAttackEvent;
import io.izzel.taboolib.common.event.PlayerKeepAliveEvent;
import io.izzel.taboolib.module.inject.PlayerContainer;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.nms.impl.Position;
import io.izzel.taboolib.module.packet.Packet;
import io.izzel.taboolib.module.packet.TPacket;
import io.izzel.taboolib.util.item.Equipments;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * @author sky
 * @since 2020-01-14 21:26
 */
public class ListenerPlayer {

    @PlayerContainer
    private static final List<String> firstList = Lists.newCopyOnWriteArrayList();

    @SuppressWarnings("ConstantConditions")
    @TPacket(type = TPacket.Type.RECEIVE)
    static boolean e(Player player, Packet packet) {
        if (packet.is("PacketPlayInUseEntity") && packet.read("action").equals("ATTACK")) {
            try {
                Entity entityById = NMS.handle().getEntityById(packet.read("a", 0));
                if (entityById != null && new PlayerAttackEvent(player, entityById).call().isCancelled()) {
                    return false;
                }
            } catch (Throwable ignored) {
            }
        }
        if (packet.is("PacketPlayInPosition")) {
            PlayerKeepAliveEvent event = new PlayerKeepAliveEvent(player, !firstList.contains(player.getName())).call();
            if (event.isFirst()) {
                firstList.add(player.getName());
            }
            return true;
        }
        if (packet.is("PacketPlayInBlockDig")) {
            Position position = TabooLibAPI.nmsFactory().generic().fromBlockPosition(packet.read("a"));
            AsyncPlayerPreBlockDigEvent.Type type = AsyncPlayerPreBlockDigEvent.Type.values()[packet.read("c", Enum.class).ordinal()];
            return new AsyncPlayerPreBlockDigEvent(player, position, BlockFace.valueOf(packet.read("b").toString().toUpperCase()), type).call().nonCancelled();
        }
        if (packet.is("PacketPlayInUseItem")) {
            Position position;
            BlockFace direction;
            Equipments hand;
            if (Version.isAfter(Version.v1_14)) {
                position = TabooLibAPI.nmsFactory().generic().fromBlockPosition(packet.reflex().read("a/c"));
                direction = BlockFace.valueOf(packet.reflex().read("a/b").toString().toUpperCase());
                hand = packet.read("b").toString().equals("MAIN_HAND") ? Equipments.HAND : Equipments.OFF_HAND;
            } else {
                position = TabooLibAPI.nmsFactory().generic().fromBlockPosition(packet.read("a"));
                direction = BlockFace.valueOf(packet.read("b").toString().toUpperCase());
                hand = packet.read("c").toString().equals("MAIN_HAND") ? Equipments.HAND : Equipments.OFF_HAND;
            }
            return new AsyncPlayerPreUseItemEvent(player, position, hand, direction).call().nonCancelled();
        }
        return true;
    }

}
