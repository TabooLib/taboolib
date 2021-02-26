package io.izzel.taboolib.client.packet.impl;

import io.izzel.taboolib.client.TabooLibClient;
import io.izzel.taboolib.client.packet.Packet;
import io.izzel.taboolib.client.packet.PacketType;

/**
 * @author sky
 * @since 2018-08-22 23:01
 */
@PacketType(name = "heartbeat")
public class PacketHeartbeat extends Packet {

    public PacketHeartbeat(int port) {
        super(port);
    }

    @Override
    public void readOnServer() {
    }

    @Override
    public void readOnClient() {
        // 更新响应时间
        TabooLibClient.setLatestResponse(System.currentTimeMillis());
        // 回应服务端
        TabooLibClient.sendPacket(new PacketAlive(TabooLibClient.getSocket().getLocalPort()));
    }
}
