package me.skymc.taboolib.socket.packet.impl;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.socket.TabooLibServer;
import me.skymc.taboolib.socket.packet.Packet;
import me.skymc.taboolib.socket.packet.PacketType;
import me.skymc.taboolib.socket.packet.PacketValue;

/**
 * @Author sky
 * @Since 2018-08-22 23:38
 */
@PacketType(name = "quit")
public class PacketQuit extends Packet {

    @PacketValue
    private String message;

    public PacketQuit(int port) {
        this(port, "connect closed.");
    }

    public PacketQuit(int port, String message) {
        super(port);
        this.message = message;
    }

    @Override
    public void readOnServer() {
        TabooLibServer.getConnection(getPort()).ifPresent(connection -> {
            // 注销连接
            TabooLibServer.getClient().remove(connection.getKey());
            // 关闭连接
            try {
                connection.getValue().getSocket().close();
            } catch (Exception ignored) {
            }
            // 提示信息
            TabooLibServer.println("Client " + getPort() + " leaved Communication Area: " + message);
        });
    }

    @Override
    public void readOnClient() {
        TLocale.sendToConsole("COMMUNICATION.CLIENT-QUITED", String.valueOf(getPort()));
    }
}
