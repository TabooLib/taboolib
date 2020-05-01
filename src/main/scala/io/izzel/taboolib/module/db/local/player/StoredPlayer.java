package io.izzel.taboolib.module.db.local.player;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.db.IHost;
import io.izzel.taboolib.module.db.local.LocalPlayer;
import io.izzel.taboolib.module.db.local.SecuredFile;
import io.izzel.taboolib.module.db.source.DBSource;
import io.izzel.taboolib.module.db.sql.SQLTable;
import io.izzel.taboolib.module.db.sql.query.Where;
import io.izzel.taboolib.module.db.sqlite.SQLiteHost;
import io.izzel.taboolib.module.inject.TFunction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @Author sky
 * @Since 2020-04-29 21:46
 */
public class StoredPlayer {

    private static IHost host;
    private static SQLTable table;
    private static DataSource dataSource;

    private static final Map<String, FileConfiguration> caches = Maps.newConcurrentMap();

    @TFunction.Init
    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("TabooLib") != null) {
            host = new SQLiteHost(new File(LocalPlayer.getFolder(), "data.db"), TabooLib.getPlugin());
            table = new SQLTable("player_data");
            try {
                dataSource = DBSource.create(host);
                table.executeUpdate("create table player_data (id integer not null primary key autoincrement, name text, data text)").dataSource(dataSource).run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static synchronized boolean find(OfflinePlayer player) {
        return table.select(Where.equals("name", LocalPlayer.toName(player))).find(dataSource);
    }

    public static synchronized FileConfiguration get(OfflinePlayer player) {
        return table.select(Where.equals("name", LocalPlayer.toName(player))).to(dataSource).resultNext(r -> SecuredFile.loadConfiguration(new String(Base64.getDecoder().decode(r.getString("data")), StandardCharsets.UTF_8))).run(new SecuredFile(), SecuredFile.class);
    }

    public static synchronized void set(OfflinePlayer player, FileConfiguration data) {
        if (find(player)) {
            table.update(Where.equals("name", LocalPlayer.toName(player))).set("data", Base64.getEncoder().encodeToString(data.saveToString().getBytes(StandardCharsets.UTF_8))).run(dataSource);
        } else {
            table.insert(LocalPlayer.toName(player), Base64.getEncoder().encodeToString(data.saveToString().getBytes(StandardCharsets.UTF_8))).run(dataSource);
        }
    }
}
