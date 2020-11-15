package io.izzel.taboolib.common.listener;

import com.google.common.collect.Lists;
import io.izzel.taboolib.common.event.PlayerAttackEvent;
import io.izzel.taboolib.common.event.PlayerKeepAliveEvent;
import io.izzel.taboolib.module.inject.PlayerContainer;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.packet.Packet;
import io.izzel.taboolib.module.packet.TPacket;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @Author sky
 * @Since 2020-01-14 21:26
 */
public class ListenerPlayer {

    @PlayerContainer
    private static final List<String> firstList = Lists.newCopyOnWriteArrayList();

    @TPacket(type = TPacket.Type.RECEIVE)
    static boolean e(Player player, Packet packet) {
        if (packet.is("PacketPlayInUseEntity") && packet.read("action").equals("ATTACK")) {
            try {
                Entity entityById = NMS.handle().getEntityById(packet.read("a", Integer.TYPE));
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
        return true;
    }

}
