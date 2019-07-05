package io.izzel.taboolib.origin.client.packet.impl;

import io.izzel.taboolib.origin.client.packet.Packet;
import io.izzel.taboolib.origin.client.packet.PacketType;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
@PacketType(name = "empty")
public class PacketEmpty extends Packet {

    public PacketEmpty(int port) {
        super(port);
    }

    @Override
    public void readOnServer() {
    }

    @Override
    public void readOnClient() {
    }
}
