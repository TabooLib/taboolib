package io.izzel.taboolib.module.db.local;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.common.loader.Startup;
import io.izzel.taboolib.module.db.local.player.LocalPlayerBridge;
import io.izzel.taboolib.module.db.local.player.LocalPlayerFile;
import io.izzel.taboolib.module.db.local.player.LocalPlayerHandler;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.util.TMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

/**
 * @Author 坏黑
 * @Since 2019-07-06 17:43
 */
public class LocalPlayer {

    private static LocalPlayerHandler handler;

    @Startup.Starting
    static void init() {
        TMap bridge = TMap.parse(TabooLib.getConfig().getString("LOCAL-PLAYER-BRIDGE", "{enable=false}"));
        if (bridge.getBoolean("enable")) {
            handler = new LocalPlayerBridge(bridge.get("client"), bridge.get("database"), bridge.get("collection"));
        } else {
            handler = new LocalPlayerFile();
        }
    }

    public static LocalPlayerHandler getHandler() {
        return handler;
    }

    public static void setHandler(LocalPlayerHandler handler) {
        LocalPlayer.handler = handler;
    }

    /**
     * 获取玩家数据（通过缓存）
     */
    public static FileConfiguration get(OfflinePlayer player) {
        return handler.get(player);
    }

    /**
     * 获取玩家数据（绕过缓存）
     */
    public static FileConfiguration get0(OfflinePlayer player) {
        return handler.get0(player);
    }

    /**
     * 上传玩家数据（绕过缓存，这个方法不会更新缓存）
     */
    public static void set0(OfflinePlayer player, FileConfiguration data) {
        handler.set0(player, data);
    }

    @TSchedule(delay = 20 * 180, period = 20 * 180, async = true)
    public static void saveFiles() {
        handler.save();
    }

    public static void save(OfflinePlayer player) {
        handler.save(player);
    }

    public static String toName(OfflinePlayer player) {
        return isUniqueIdMode() ? player.getUniqueId().toString() : player.getName();
    }

    public static OfflinePlayer toPlayer(String name) {
        return isUniqueIdMode() ? Bukkit.getOfflinePlayer(UUID.fromString(name)) : Bukkit.getOfflinePlayer(name);
    }

    public static boolean isUniqueIdMode() {
        return TabooLib.getConfig().getBoolean("LOCAL-PLAYER-UUID");
    }
}
