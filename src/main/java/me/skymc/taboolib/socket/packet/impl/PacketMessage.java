package me.skymc.taboolib.socket.packet.impl;

import com.google.gson.JsonObject;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.socket.TabooLibServer;
import me.skymc.taboolib.socket.packet.Packet;
import me.skymc.taboolib.socket.packet.PacketType;
import me.skymc.taboolib.socket.packet.PacketValue;
import org.bukkit.Bukkit;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
@PacketType(name = "message")
public class PacketMessage extends Packet {

    @PacketValue
    private String message;

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
