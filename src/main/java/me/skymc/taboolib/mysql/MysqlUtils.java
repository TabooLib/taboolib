package me.skymc.taboolib.mysql;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.mysql.protect.MySQLConnection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.util.concurrent.CopyOnWriteArrayList;

public class MysqlUtils {

    public final static CopyOnWriteArrayList<MySQLConnection> CONNECTIONS = new CopyOnWriteArrayList<>();

    @Deprecated
    public static MysqlConnection getMysqlConnectionFromConfiguration(FileConfiguration conf, String key) {
        return new MysqlConnection(conf.getString(key + ".host"), conf.getString(key + ".port"), conf.getString(key + ".database"), conf.getString(key + ".user"), conf.getString(key + ".pass"));
    }

    public static MySQLConnection getMySQLConnectionFromConfiguration(FileConfiguration conf, String key) {
        return getMySQLConnectionFromConfiguration(conf, key, 60, Main.getInst());
    }

    public static MySQLConnection getMySQLConnectionFromConfiguration(FileConfiguration conf, String key, int recheck, Plugin plugin) {
        MySQLConnection connection = getMySQLConnectionExists(conf, key);
        if (connection == null) {
            connection = new MySQLConnection(conf.getString(key + ".url"), conf.getString(key + ".user"), conf.getString(key + ".port"), conf.getString(key + ".password"), conf.getString(key + ".database"), recheck, plugin);
            if (connection.isConnection()) {
                CONNECTIONS.add(connection);
                TLocale.Logger.info("MYSQL-CONNECTION.SUCCESS-REGISTERED", plugin.getName());
            }
        } else {
            TLocale.Logger.info("MYSQL-CONNECTION.SUCCESS-REGISTERED-EXISTS", plugin.getName(), connection.getPlugin().getName());
        }
        return connection;
    }

    private static MySQLConnection getMySQLConnectionExists(FileConfiguration conf, String key) {
        return CONNECTIONS.stream().filter(connection -> isSameConnection(conf, key, connection)).findFirst().orElse(null);
    }

    private static boolean isSameConnection(FileConfiguration conf, String key, MySQLConnection connection) {
        return conversionHost(connection.getUrl()).equals(conversionHost(conf.getString(key + ".url", "localhost"))) && connection.getDatabase().equals(conf.getString(key + ".database"));
    }

    private static String conversionHost(String host) {
        return "localhost".equals(host) ? "127.0.0.1" : host;
    }
}
