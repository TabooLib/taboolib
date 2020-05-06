package io.izzel.taboolib.module.db.local;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.common.loader.Startup;
import io.izzel.taboolib.module.db.IHost;
import io.izzel.taboolib.module.db.source.DBSource;
import io.izzel.taboolib.module.db.sql.SQLTable;
import io.izzel.taboolib.module.db.sql.query.Where;
import io.izzel.taboolib.module.db.sqlite.SQLiteHost;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author 坏黑
 * @Since 2019-07-06 17:43
 */
public class LocalPlayer {

    private static final Map<String, FileConfiguration> files = Maps.newConcurrentMap();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static IHost host;
    private static SQLTable table;
    private static DataSource dataSource;

    @Startup.Starting
    public static void init() {
        host = new SQLiteHost(Files.file(LocalPlayer.getFolder(), "v2/data.db"), TabooLib.getPlugin());
        table = new SQLTable("player_data");
        try {
            dataSource = DBSource.create(host);
            table.executeUpdate("create table if not exists player_data (name text primary key, data text)").dataSource(dataSource).run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static FileConfiguration get0(OfflinePlayer player) {
        File file = toFile(toName(player));
        if (file.exists()) {
            try {
                return SecuredFile.loadConfiguration(file);
            } finally {
                Files.copy(file, Files.file(getFolder(), toName(player) + ".bak"));
                Files.deepDelete(file);
            }
        }
        try {
            return executor.submit(() -> table.select(Where.equals("name", LocalPlayer.toName(player))).to(dataSource).resultNext(r -> SecuredFile.loadConfiguration(new String(Base64.getDecoder().decode(r.getString("data")), StandardCharsets.UTF_8))).run(new SecuredFile(), SecuredFile.class)).get();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new SecuredFile();
    }

    public static void set0(OfflinePlayer player, FileConfiguration data) {
        executor.submit(() -> {
            if (table.select(Where.equals("name", LocalPlayer.toName(player))).find(dataSource)) {
                table.update(Where.equals("name", LocalPlayer.toName(player))).set("data", Base64.getEncoder().encodeToString(data.saveToString().getBytes(StandardCharsets.UTF_8))).run(dataSource);
            } else {
                table.insert(LocalPlayer.toName(player), Base64.getEncoder().encodeToString(data.saveToString().getBytes(StandardCharsets.UTF_8))).run(dataSource);
            }
        });
    }

    public static FileConfiguration get(OfflinePlayer player) {
        return files.computeIfAbsent(toName(player), n -> get0(player));
    }

    @TSchedule(delay = 20 * 180, period = 20 * 180, async = true)
    public static void saveFiles() {
        files.forEach((name, file) -> {
            OfflinePlayer player = toPlayer(name);
            if (!player.isOnline()) {
                files.remove(name);
            }
            set0(player, file);
        });
    }

    public static File getFolder() {
        return Files.folder(TabooLib.getConfig().getString("LOCAL-PLAYER"));
    }

    public static File toFile(String name) {
        return new File(getFolder(), name + ".yml");
    }

    public static String toName(OfflinePlayer player) {
        return isUniqueIdMode() ? player.getUniqueId().toString() : player.getName();
    }

    public static boolean isUniqueIdMode() {
        return TabooLib.getConfig().getBoolean("LOCAL-PLAYER-UUID");
    }

    public static OfflinePlayer toPlayer(String name) {
        return isUniqueIdMode() ? Bukkit.getOfflinePlayer(UUID.fromString(name)) : Bukkit.getOfflinePlayer(name);
    }
}
