package io.izzel.taboolib.client.packet.impl;

import io.izzel.taboolib.client.TabooLibServer;
import io.izzel.taboolib.client.packet.Packet;
import io.izzel.taboolib.client.packet.PacketType;
import io.izzel.taboolib.module.locale.TLocale;

/**
 * @author sky
 * @since 2018-08-22 23:38
 */
@PacketType(name = "join")
public class PacketJoin extends Packet {

    public PacketJoin(int port) {
        super(port);
    }

    @Override
    public void readOnServer() {
        TabooLibServer.println("Client " + getPort() + " joined Communication Area.");
    }

    @Override
    public void readOnClient() {
        TLocale.sendToConsole("COMMUNICATION.CLIENT-JOINED", String.valueOf(getPort()));
    }
}
