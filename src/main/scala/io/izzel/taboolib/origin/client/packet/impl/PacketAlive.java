package io.izzel.taboolib.origin.client.packet.impl;

import io.izzel.taboolib.origin.client.TabooLibServer;
import io.izzel.taboolib.origin.client.packet.Packet;
import io.izzel.taboolib.origin.client.packet.PacketType;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
@PacketType(name = "alive")
public class PacketAlive extends Packet {

    public PacketAlive(int port) {
        super(port);
    }

    @Override
    public void readOnServer() {
        TabooLibServer.getConnection(getPort()).ifPresent(connect -> connect.getValue().setLatestResponse(System.currentTimeMillis()));
    }

    @Override
    public void readOnClient() {
    }
}
