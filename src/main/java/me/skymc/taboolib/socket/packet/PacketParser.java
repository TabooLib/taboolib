package me.skymc.taboolib.socket.packet;

import com.google.gson.JsonObject;
import me.skymc.taboolib.fileutils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author sky
 * @Since 2018-08-22 23:07
 */
public class PacketParser {

    private List<Class<?>> packets = new ArrayList<>();

    public PacketParser() {
        FileUtils.getClasses(PacketParser.class).stream().filter(clazz -> clazz.isAnnotationPresent(PacketType.class)).forEach(clazz -> packets.add(clazz));
    }

    public Packet parser(JsonObject json) {
        if (!json.has("packet")) {
            return null;
        }
        String packetType = json.get("packet").getAsString();
        Optional<Class<?>> packetFind = packets.stream().filter(packet -> packet.getAnnotation(PacketType.class).name().equals(packetType)).findFirst();
        if (!packetFind.isPresent()) {
            return null;
        }
        try {
            Packet packetObject = (Packet) packetFind.get().getConstructor(Integer.TYPE).newInstance(json.get("port").getAsInt());
            packetObject.unSerialize(json);
            return packetObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public List<Class<?>> getPackets() {
        return packets;
    }
}
