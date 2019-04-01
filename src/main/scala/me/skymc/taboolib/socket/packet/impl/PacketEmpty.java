package me.skymc.taboolib.socket.packet.impl;

import me.skymc.taboolib.socket.packet.Packet;
import me.skymc.taboolib.socket.packet.PacketType;

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
