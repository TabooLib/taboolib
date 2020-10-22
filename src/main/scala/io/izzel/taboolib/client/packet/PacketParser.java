package io.izzel.taboolib.client.packet;

import com.google.gson.JsonObject;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.util.Files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @Author sky
 * @Since 2018-08-22 23:07
 */
public class PacketParser {

    private final List<Class<?>> packets = new ArrayList<>();

    public PacketParser() {
        Files.getClasses(TabooLib.getPlugin()).stream().filter(clazz -> clazz.isAnnotationPresent(PacketType.class)).forEach(packets::add);
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
            Arrays.stream(packetObject.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(PacketValue.class)).forEach(field -> {
                field.setAccessible(true);
                try {
                    switch (field.getType().getSimpleName().toLowerCase()) {
                        case "double":
                            field.set(packetObject, json.get(field.getName()).getAsDouble());
                            break;
                        case "long":
                            field.set(packetObject, json.get(field.getName()).getAsLong());
                            break;
                        case "short":
                            field.set(packetObject, json.get(field.getName()).getAsShort());
                            break;
                        case "boolean":
                            field.set(packetObject, json.get(field.getName()).getAsBoolean());
                            break;
                        case "string":
                            field.set(packetObject, json.get(field.getName()).getAsString());
                            break;
                        case "number":
                            field.set(packetObject, json.get(field.getName()).getAsNumber());
                            break;
                        case "int":
                        case "integer":
                            field.set(packetObject, json.get(field.getName()).getAsInt());
                            break;
                        case "char":
                        case "character":
                            field.set(packetObject, json.get(field.getName()).getAsCharacter());
                            break;
                        default:
                            System.out.println("UnSerialize: Invalid packet value: " + field.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return packetObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Class<?>> getPackets() {
        return packets;
    }
}
