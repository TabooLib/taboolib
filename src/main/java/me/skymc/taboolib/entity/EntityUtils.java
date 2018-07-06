package me.skymc.taboolib.entity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.exception.PluginNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class EntityUtils implements Listener {

    private static Entity lastSpawnedEntity = null;

    public static Entity getLastSpawnedEntity() {
        return lastSpawnedEntity;
    }

    /**
     * 根据 UUID 获取生物
     *
     * @param u
     * @return
     */
    public static Entity getEntityWithUUID(UUID u) {
        return Bukkit.getWorlds().stream().flatMap(w -> w.getLivingEntities().stream()).filter(e -> e.getUniqueId().equals(u)).findFirst().orElse(null);
    }

    /**
     * 根据 UUID 获取生物（单世界）
     *
     * @param u
     * @param world
     * @return
     */
    public static Entity getEntityWithUUID_World(UUID u, World world) {
        return world.getLivingEntities().stream().filter(e -> e.getUniqueId().equals(u)).findFirst().orElse(null);
    }

    /**
     * 设置生物发光（ProcotolLib）
     *
     * @param player
     * @param entity
     */
    public static void addGlow(Player player, Entity entity) {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            TLocale.sendToConsole("ENTITY-UTILS.NOTFOUND-PROTOCOLLIB");
        }
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
    public static void delGlow(Player player, Entity entity) {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            TLocale.sendToConsole("ENTITY-UTILS.NOTFOUND-PROTOCOLLIB");
        }
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

    @EventHandler
    public void spawn(EntitySpawnEvent e) {
        lastSpawnedEntity = e.getEntity();
    }

}
