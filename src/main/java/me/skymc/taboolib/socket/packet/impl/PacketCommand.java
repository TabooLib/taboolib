package me.skymc.taboolib.socket.packet.impl;

import com.google.gson.JsonObject;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.socket.TabooLibServer;
import me.skymc.taboolib.socket.packet.Packet;
import me.skymc.taboolib.socket.packet.PacketType;
import org.bukkit.Bukkit;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
@PacketType(name = "command")
public class PacketCommand extends Packet {

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

    @Override
    public void readOnClient() {
    }

    @Override
    public void serialize(JsonObject json) {
        json.addProperty("command", this.command);
    }

    @Override
    public void unSerialize(JsonObject json) {
        this.command = json.get("command").getAsString();
    }
}
