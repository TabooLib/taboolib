package me.skymc.taboolib.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import me.skymc.taboolib.methods.MethodsUtils;

public class EntityUtils implements Listener{
	
	public static Entity lastSpawned = null;
	
	@EventHandler
	public void spawn(EntitySpawnEvent e) {
		lastSpawned = e.getEntity();
	}
	
	public static Entity getEntityWithUUID(UUID u) {
		for (World w : Bukkit.getWorlds()) {
			for (Entity e : w.getLivingEntities()) {
				if (e.getUniqueId().equals(u)) {
					return e;
				}
			}
		}
		return null;
	}
	
	public static Entity getEntityWithUUID_World(UUID u, World world) {
		for (Entity e : world.getLivingEntities()) {
			if (e.getUniqueId().equals(u)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 设置生物发光（ProcotolLib）
	 * 
	 * @param player
	 * @param entity
	 */
	public static void addGlow(Player player,Entity entity) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        watcher.setEntity(player);
        watcher.setObject(0, serializer, (byte) (0x40));
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        try {
        	ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
	/**
	 * 取消生物发光（ProcotolLib）
	 * 
	 * @param player
	 * @param entity
	 */
    public static void delGlow(Player player,Entity entity) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        watcher.setEntity(player);
        watcher.setObject(0, serializer, (byte) (0x0));
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        try {
        	ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
