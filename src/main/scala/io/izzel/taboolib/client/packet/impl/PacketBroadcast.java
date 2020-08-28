package io.izzel.taboolib.client.packet.impl;

import io.izzel.taboolib.client.packet.Packet;
import io.izzel.taboolib.client.packet.PacketType;
import io.izzel.taboolib.client.packet.PacketValue;
import io.izzel.taboolib.client.packet.event.TabooClientPacketBroadcast;
import org.bukkit.Bukkit;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
@PacketType(name = "broadcast")
public class PacketBroadcast extends Packet {

    @PacketValue
    private String value;

    public PacketBroadcast(int port) {
        super(port);
    }

    public PacketBroadcast(String command) {
        super(Bukkit.getPort());
        this.value = command;
    }

    @Override
    public void readOnServer() {
        new TabooClientPacketBroadcast(value).call();
    }
}
