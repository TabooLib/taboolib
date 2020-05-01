package io.izzel.taboolib.module.db.local;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * @Author 坏黑
 * @Since 2019-07-06 17:43
 */
public class LocalPlayer {

    private static final Map<String, FileConfiguration> files = Maps.newConcurrentMap();

    public static FileConfiguration get(OfflinePlayer player) {
        return TabooLibAPI.isOriginLoaded() ? TabooLibAPI.getPluginBridge().taboolibGetPlayerData(toName(player)) : files.computeIfAbsent(toName(player), n -> Files.load(toFile(n)));
    }

    @TSchedule(delay = 20 * 30, period = 20 * 30, async = true)
    public static void saveFiles() {
        files.forEach((name, file) -> {
            try {
                file.save(toFile(name));
            } catch (NullPointerException ignored) {
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @TSchedule(delay = 20 * 30, period = 20 * 30, async = true)
    public static void checkFile() {
        files.forEach((name, file) -> {
            if (toPlayer(name) == null) {
                try {
                    files.remove(name).save(toFile(name));
                } catch (NullPointerException ignored) {
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    public static File getFolder() {
        return Files.folder(TabooLib.getConfig().getString("LOCAL-PLAYER"));
    }

    public static File toFile(String name) {
        return Files.file(getFolder(), name + ".yml");
    }

    public static String toName(OfflinePlayer player) {
        return isUniqueIdMode() ? player.getUniqueId().toString() : player.getName();
    }

    public static boolean isUniqueIdMode() {
        return TabooLib.getConfig().getBoolean("LOCAL-PLAYER-UUID");
    }

    public static Player toPlayer(String name) {
        return isUniqueIdMode() ? Bukkit.getPlayer(UUID.fromString(name)) : Bukkit.getPlayerExact(name);
    }
}
