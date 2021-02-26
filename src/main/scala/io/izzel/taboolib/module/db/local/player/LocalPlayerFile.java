package io.izzel.taboolib.module.db.local.player;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.db.local.LocalPlayer;
import io.izzel.taboolib.module.db.local.SecuredFile;
import io.izzel.taboolib.module.db.source.DBSource;
import io.izzel.taboolib.module.db.sql.SQLTable;
import io.izzel.taboolib.module.db.sql.query.Where;
import io.izzel.taboolib.module.db.sqlite.SQLiteHost;
import io.izzel.taboolib.util.Files;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 玩家数据 SQLite 储存方式
 *
 * @author sky
 * @since 2020-07-03 18:21
 */
public class LocalPlayerFile extends LocalPlayerHandler {

    private final Map<String, FileConfiguration> files = Maps.newConcurrentMap();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SQLiteHost host;
    private final SQLTable table;
    private DataSource dataSource;

    public LocalPlayerFile() {
        host = new SQLiteHost(Files.file(getFolder(), "v2/data.db"), TabooLib.getPlugin());
        table = new SQLTable("player_data");
        try {
            dataSource = DBSource.create(host);
            table.executeUpdate("create table if not exists player_data (name varchar(32) primary key, data text)").dataSource(dataSource).run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void save() {
        files.forEach((name, file) -> {
            OfflinePlayer player = LocalPlayer.toPlayer(name);
            if (!player.isOnline()) {
                files.remove(name);
            }
            set0(player, file);
        });
    }

    @Override
    public void save(OfflinePlayer player) {
        FileConfiguration file;
        if (player.isOnline()) {
            file = files.get(LocalPlayer.toName(player));
        } else {
            file = files.remove(LocalPlayer.toName(player));
        }
        set0(player, file);
    }

    @Override
    public FileConfiguration get(OfflinePlayer player) {
        return files.computeIfAbsent(LocalPlayer.toName(player), n -> get0(player));
    }

    @Override
    public FileConfiguration get0(OfflinePlayer player) {
        File file = toFile(LocalPlayer.toName(player));
        if (file.exists()) {
            try {
                return SecuredFile.loadConfiguration(file);
            } finally {
                Files.copy(file, Files.file(getFolder(), LocalPlayer.toName(player) + ".bak"));
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

    @Override
    public void set0(OfflinePlayer player, FileConfiguration data) {
        executor.submit(() -> {
            if (table.select(Where.equals("name", LocalPlayer.toName(player))).find(dataSource)) {
                table.update(Where.equals("name", LocalPlayer.toName(player))).set("data", Base64.getEncoder().encodeToString(data.saveToString().getBytes(StandardCharsets.UTF_8))).run(dataSource);
            } else {
                table.insert(LocalPlayer.toName(player), Base64.getEncoder().encodeToString(data.saveToString().getBytes(StandardCharsets.UTF_8))).run(dataSource);
            }
        });
    }

    public File getFolder() {
        return Files.folder(TabooLib.getConfig().getString("LOCAL-PLAYER"));
    }

    public File toFile(String name) {
        return new File(getFolder(), name + ".yml");
    }

    public Map<String, FileConfiguration> getFiles() {
        return files;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public SQLiteHost getHost() {
        return host;
    }

    public SQLTable getTable() {
        return table;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
