package me.skymc.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.mysql.builder.SQLColumn;
import me.skymc.taboolib.mysql.builder.SQLColumnType;
import me.skymc.taboolib.mysql.builder.SQLHost;
import me.skymc.taboolib.mysql.builder.SQLTable;
import me.skymc.taboolib.mysql.hikari.HikariHandler;
import me.skymc.taboolib.string.StringUtils;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 * @Author sky
 * @Since 2018-08-23 17:15
 */
public class TabooLibDatabase {

    private static SQLHost host;
    private static DataSource dataSource;
    private static HashMap<String, SQLTable> tables = new HashMap<>();

    static void init() {
        if (Main.getStorageType() != Main.StorageType.SQL) {
            return;
        }
        // 数据库地址
        host = new SQLHost(
                // 地址
                Main.getInst().getConfig().getString("MYSQL.HOST"),
                // 用户
                Main.getInst().getConfig().getString("MYSQL.USER"),
                // 端口
                Main.getInst().getConfig().getString("MYSQL.POST"),
                // 密码
                Main.getInst().getConfig().getString("MYSQL.PASSWORD"),
                // 数据库
                Main.getInst().getConfig().getString("MYSQL.DATABASE"), TabooLib.instance());
        // 连接数据库
        try {
            dataSource = HikariHandler.createDataSource(host);
        } catch (Exception ignored) {
            TLocale.Logger.error("NOTIFY.ERROR-CONNECTION-FAIL");
            return;
        }
        // 创建各项数据表
        createTableWithPlayerData();
        createTableWithPluginData();
        createTableWithServerUUID();
    }

    /**
     * 创建玩家数据表
     */
    static void createTableWithPlayerData() {
        SQLTable table = new SQLTable(Main.getTablePrefix() + "_playerdata", SQLColumn.PRIMARY_KEY_ID, new SQLColumn(SQLColumnType.TEXT, "username"), new SQLColumn(SQLColumnType.TEXT, "configuration"));
        table.executeUpdate(table.createQuery()).dataSource(dataSource).run();
        tables.put("playerdata", table);
    }

    /**
     * 创建插件数据表
     */
    static void createTableWithPluginData() {
        SQLTable table = new SQLTable(Main.getTablePrefix() + "_plugindata", SQLColumn.PRIMARY_KEY_ID, new SQLColumn(SQLColumnType.TEXT, "name"), new SQLColumn(SQLColumnType.TEXT, "variable"), new SQLColumn(SQLColumnType.TEXT, "upgrade"));
        table.executeUpdate(table.createQuery()).dataSource(dataSource).run();
        tables.put("plugindata", table);
    }

    /**
     * 创建服务器数据表
     */
    static void createTableWithServerUUID() {
        SQLTable table = new SQLTable(Main.getTablePrefix() + "_serveruuid", SQLColumn.PRIMARY_KEY_ID, new SQLColumn(SQLColumnType.TEXT, "uuid"), new SQLColumn(SQLColumnType.TEXT, "hash"));
        table.executeUpdate(table.createQuery()).dataSource(dataSource).run();
        tables.put("serveruuid", table);
        // 获取当前服务器信息
        String hash = getServerHash(TabooLib.getServerUID());
        if (hash == null) {
            // 写入序列号
            table.executeUpdate("insert into " + table.getTableName() + " values(null, ?, ?)")
                    .dataSource(dataSource)
                    .statement(s -> {
                        s.setString(1, TabooLib.getServerUID());
                        s.setString(2, StringUtils.hashKeyForDisk(Main.getInst().getDataFolder().getPath()));
                    }).run();
        } else if (!hash.equals(StringUtils.hashKeyForDisk(Main.getInst().getDataFolder().getPath()))) {
            TLocale.Logger.error("NOTIFY.ERROR-SERVER-KEY");
            TabooLib.resetServerUID();
            Bukkit.shutdown();
        }
    }

    /**
     * 获取服务器序列号对应的目录哈希值
     *
     * @param uuid 服务器序列号
     * @return 目录哈希值
     */
    public static String getServerHash(String uuid) {
        SQLTable table = tables.get("serveruuid");
        return table.executeQuery("select * from " + table.getTableName() + " where uuid = ?")
                .dataSource(dataSource)
                .statement(s -> s.setString(1, uuid))
                .resultNext(r -> r.getString("hash"))
                .run(null, "");
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static SQLHost getHost() {
        return host;
    }

    public static HashMap<String, SQLTable> getTables() {
        return tables;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
