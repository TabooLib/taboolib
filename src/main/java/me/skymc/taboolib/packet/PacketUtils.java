package me.skymc.taboolib.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketUtils {

    public enum EntityStatus {
        FIRE, CROUCHED, UNUSED1, UNUSED2, SPRINTING, INVISIBLE, GLOWING, ELYTRA
    }

    public static void sendPacketEntityStatus(Entity entity, EntityStatus status, Player... players) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        switch (status) {
            case FIRE:
                watcher.setObject(0, serializer, (byte) 0x01);
                break;
            case CROUCHED:
                watcher.setObject(0, serializer, (byte) 0x02);
                break;
            case UNUSED1:
                watcher.setObject(0, serializer, (byte) 0x04);
                break;
            case SPRINTING:
                watcher.setObject(0, serializer, (byte) 0x08);
                break;
            case UNUSED2:
                watcher.setObject(0, serializer, (byte) 0x10);
                break;
            case INVISIBLE:
                watcher.setObject(0, serializer, (byte) 0x20);
                break;
            case GLOWING:
                watcher.setObject(0, serializer, (byte) 0x40);
                break;
            case ELYTRA:
                watcher.setObject(0, serializer, (byte) 0x80);
                break;
            default:
                watcher.setObject(0, serializer, (byte) 0x00);
                break;
        }
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        try {
            for (Player player : players) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendPacketEntityCustomName(Entity entity, String value, Player... players) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(String.class);
        watcher.setObject(2, serializer, value == null ? "" : value);
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        try {
            for (Player player : players) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
