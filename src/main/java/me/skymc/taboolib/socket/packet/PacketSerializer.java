package me.skymc.taboolib.socket.packet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.skymc.taboolib.socket.packet.impl.PacketEmpty;

/**
 * @Author sky
 * @Since 2018-08-22 23:32
 */
public class PacketSerializer {

    private static PacketParser parser = new PacketParser();

    public static String serialize(Packet packet) {
        JsonObject json = new JsonObject();
        json.addProperty("uid", packet.getUid());
        json.addProperty("port", packet.getPort());
        json.addProperty("packet", packet.getClass().getAnnotation(PacketType.class).name());
        packet.serialize(json);
        return json.toString();
    }

    public static Packet unSerialize(String origin) {
        Packet packet = null;
        try {
            packet = parser.parser((JsonObject) new JsonParser().parse(origin));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return packet == null ? new PacketEmpty(0) : packet;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static PacketParser getParser() {
        return parser;
    }
}
