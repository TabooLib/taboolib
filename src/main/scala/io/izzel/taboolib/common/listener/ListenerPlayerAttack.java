package io.izzel.taboolib.common.listener;

import io.izzel.taboolib.common.event.PlayerAttackEvent;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.packet.Packet;
import io.izzel.taboolib.module.packet.TPacket;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * @Author sky
 * @Since 2020-01-14 21:26
 */
public class ListenerPlayerAttack {

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
        return true;
    }

}
