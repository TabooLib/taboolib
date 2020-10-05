package io.izzel.taboolib.client.packet.impl;

import io.izzel.taboolib.client.TabooLibServer;
import io.izzel.taboolib.client.packet.Packet;
import io.izzel.taboolib.client.packet.PacketType;
import io.izzel.taboolib.client.packet.PacketValue;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.Bukkit;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
@PacketType(name = "message")
public class PacketMessage extends Packet {

    @PacketValue
    private final String message;

    public PacketMessage(int port) {
        this(port, "none");
    }

    public PacketMessage(int port, String message) {
        super(port);
        this.message = message;
    }

    public PacketMessage(String message) {
        super(Bukkit.getPort());
        this.message = message;
    }

    @Override
    public void readOnServer() {
        TabooLibServer.println(getPort() + ": " + message);
    }

    @Override
    public void readOnClient() {
        TLocale.sendToConsole("COMMUNICATION.PACKET-MESSAGE", String.valueOf(getPort()), message);
    }
}
