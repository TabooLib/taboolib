package io.izzel.taboolib.module.db.local.player;

import io.izzel.taboolib.cronus.bridge.CronusBridge;
import io.izzel.taboolib.cronus.bridge.database.BridgeCollection;
import io.izzel.taboolib.cronus.bridge.database.BridgeData;
import io.izzel.taboolib.cronus.bridge.database.IndexType;
import io.izzel.taboolib.module.db.local.LocalPlayer;
import io.izzel.taboolib.module.inject.TListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 玩家数据 MongoDB 储存方式
 *
 * @author sky
 * @since 2020-07-03 18:31
 */
public class LocalPlayerBridge extends LocalPlayerHandler {

    private final String client;
    private final String database;
    private final String collection;
    private final BridgeCollection bridgeCollection;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public LocalPlayerBridge(String client, String database, String collection) {
        this.client = client;
        this.database = database;
        this.collection = collection;
        this.bridgeCollection = CronusBridge.get(client, database, collection, LocalPlayer.isUniqueIdMode() ? IndexType.UUID : IndexType.USERNAME);
    }

    public void saveAsync(OfflinePlayer player) {
        executor.submit(() -> save(player));
    }

    @Override
    public void save() {
        Bukkit.getOnlinePlayers().forEach(this::saveAsync);
    }

    @Override
    public void save(OfflinePlayer player) {
        try {
            this.bridgeCollection.update(LocalPlayer.toName(player));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public FileConfiguration get(OfflinePlayer player) {
        try {
            return this.bridgeCollection.get(LocalPlayer.toName(player));
        } finally {
            saveAsync(player);
        }
    }

    @Override
    public FileConfiguration get0(OfflinePlayer player) {
        return this.bridgeCollection.get(LocalPlayer.toName(player), false);
    }

    @Override
    public void set0(OfflinePlayer player, FileConfiguration file) {
        this.bridgeCollection.update(LocalPlayer.toName(player), new BridgeData(LocalPlayer.toName(player), file));
    }

    public String getClient() {
        return client;
    }

    public String getDatabase() {
        return database;
    }

    public String getCollection() {
        return collection;
    }

    public BridgeCollection getBridgeCollection() {
        return bridgeCollection;
    }

    @TListener
    static class Events implements Listener {

        @EventHandler
        public void e(PlayerQuitEvent e) {
            if (LocalPlayer.getHandler() instanceof LocalPlayerBridge) {
                ((LocalPlayerBridge) LocalPlayer.getHandler()).saveAsync(e.getPlayer());
            }
        }
    }
}
