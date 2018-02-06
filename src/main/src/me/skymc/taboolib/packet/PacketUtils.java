package me.skymc.taboolib.packet;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class PacketUtils {
	
	public enum EntityStatus {
		
		FIRE, CROUCHED, UNUSED1, UNUSED2, SPRINTING, INVISIBLE, GLOWING, ELYTRA
	}
	
	public static void sendPacketEntityStatus(Entity entity, EntityStatus status, Player... players) {
		PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entity.getEntityId());
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
		if (status == EntityStatus.FIRE) {
			watcher.setObject(0, serializer, (byte) 0x01);
		}
		else if (status == EntityStatus.CROUCHED) {
			watcher.setObject(0, serializer, (byte) 0x02);
		}
		else if (status == EntityStatus.UNUSED1) {
			watcher.setObject(0, serializer, (byte) 0x04);
		}
		else if (status == EntityStatus.SPRINTING) {
			watcher.setObject(0, serializer, (byte) 0x08);
		}
		else if (status == EntityStatus.UNUSED2) {
			watcher.setObject(0, serializer, (byte) 0x10);
		}
		else if (status == EntityStatus.INVISIBLE) {
			watcher.setObject(0, serializer, (byte) 0x20);
		}
		else if (status == EntityStatus.GLOWING) {
			watcher.setObject(0, serializer, (byte) 0x40);
		}
		else if (status == EntityStatus.ELYTRA) {
			watcher.setObject(0, serializer, (byte) 0x80);
		}
		else {
			watcher.setObject(0, serializer, (byte) 0x00);
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
