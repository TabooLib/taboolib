package io.izzel.taboolib.cronus.bridge;

import com.google.common.collect.Maps;
import io.izzel.taboolib.cronus.bridge.database.BridgeCollection;
import io.izzel.taboolib.cronus.bridge.database.BridgeDatabase;
import io.izzel.taboolib.cronus.bridge.database.IndexType;
import io.izzel.taboolib.module.inject.TListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

/**
 * @Author sky
 * @Since 2020-07-03 14:05
 */
public class CronusBridge {

    private static final Map<String, BridgeDatabase> databaseMap = Maps.newConcurrentMap();

    public static Map<String, BridgeDatabase> getDatabaseMap() {
        return databaseMap;
    }

    public static BridgeDatabase get(String client, String database) {
        return databaseMap.computeIfAbsent(client + ";" + database, i -> new BridgeDatabase(client, database));
    }

    public static BridgeCollection get(String client, String database, String collection) {
        return get(client, database).get(collection);
    }

    public static BridgeCollection get(String client, String database, String collection, IndexType indexType) {
        return get(client, database).get(collection, indexType);
    }

    public static void release(Player player) {
        for (BridgeDatabase database : databaseMap.values()) {
            for (BridgeCollection collection : database.getCollectionMap().values()) {
                if (collection.getIndexType() == IndexType.USERNAME) {
                    collection.release(player.getName());
                } else if (collection.getIndexType() == IndexType.UUID) {
                    collection.release(player.getUniqueId().toString());
                }
            }
        }
    }

    @TListener
    static class Events implements Listener {

        @EventHandler
        public void e(PlayerQuitEvent e) {
            CronusBridge.release(e.getPlayer());
        }
    }
}
