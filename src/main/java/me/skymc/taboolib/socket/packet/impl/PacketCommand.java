package me.skymc.taboolib.socket.packet.impl;

import me.skymc.taboolib.socket.TabooLibServer;
import me.skymc.taboolib.socket.packet.Packet;
import me.skymc.taboolib.socket.packet.PacketType;
import me.skymc.taboolib.socket.packet.PacketValue;
import org.bukkit.Bukkit;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
@PacketType(name = "command")
public class PacketCommand extends Packet {

    @PacketValue
    private String command;

    public PacketCommand(int port) {
        super(port);
    }

    public PacketCommand(String command) {
        super(Bukkit.getPort());
        this.command = command;
    }

    @Override
    public void readOnServer() {
        String[] args = command.split(" ");
        if (args[0].equalsIgnoreCase("online")) {
            TabooLibServer.sendPacket(new PacketMessage(0, "Online: " + TabooLibServer.getClient().size()));
        } else {
            TabooLibServer.sendPacket(new PacketMessage(0, "Invalid arguments."));
        }
    }
}
