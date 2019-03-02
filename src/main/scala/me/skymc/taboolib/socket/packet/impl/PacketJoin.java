package me.skymc.taboolib.socket.packet.impl;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.socket.TabooLibServer;
import me.skymc.taboolib.socket.packet.Packet;
import me.skymc.taboolib.socket.packet.PacketType;

/**
 * @Author sky
 * @Since 2018-08-22 23:38
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
