package me.skymc.taboolib.translateuuid;

import com.ilummc.tlib.logger.TLogger;
import com.ilummc.tlib.util.Strings;
import com.zaxxer.hikari.HikariDataSource;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.json.JSONObject;
import me.skymc.taboolib.mysql.builder.*;
import me.skymc.taboolib.mysql.hikari.HikariHandler;
import me.skymc.taboolib.nms.NMSUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author sky
 * @Since 2018-06-21 22:16
 */
public class TranslateUUID {

    private static FileConfiguration settings;
    private static HikariDataSource dataSource;

    private static SQLHost sqlHost;
    private static SQLTable sqlTable;

    private static final String INSERT_PLAYER_DATA = "insert into {0} values(null, ?, ?)";
    private static final String SELECT_WITH_UUID = "select * from {0} where uuid = ?";
    private static final String SELECT_WITH_USERNAME = "select * from {0} where username = ?";
    private static final String UPDATE_PLAYER_USERNAME = "update {0} set username = ? where uuid = ?";

    private static boolean enabled = false;

    public static void init() {
        settings = ConfigUtils.saveDefaultConfig(Main.getInst(), "translateuuid.yml");
        if (!settings.getBoolean("Enable")) {
            enabled = false;
            return;
        }
        sqlHost = new SQLHost(settings.getConfigurationSection("Database"), Main.getInst());
        sqlTable = new SQLTable(settings.getString("Database.table"), SQLColumn.PRIMARY_KEY_ID, new SQLColumn(SQLColumnType.TEXT, "uuid"), new SQLColumn(SQLColumnType.TEXT, "username"));
        try {
            dataSource = HikariHandler.createDataSource(sqlHost, null);
            createTable();
        } catch (Exception e) {
            TLogger.getGlobalLogger().error("Database connected fail: " + e.toString());
        }
        enabled = true;
    }

    public static void cancel() {
        HikariHandler.closeDataSource(sqlHost);
    }

    public static String translateUUID(String username) {
        try (Connection connection = dataSource.getConnection()) {
            return translateInternal(connection, username, "uuid", SELECT_WITH_USERNAME);
        } catch (SQLException e) {
            TLogger.getGlobalLogger().error("Database error: " + e.toString());
            return null;
        }
    }

    public static String translateUsername(UUID uuid) {
        try (Connection connection = dataSource.getConnection()) {
            return translateInternal(connection, uuid.toString(), "username", SELECT_WITH_UUID);
        } catch (SQLException e) {
            TLogger.getGlobalLogger().error("Database error: " + e.toString());
            return null;
        }
    }

    public static void updateUsername(UUID uuid, String username) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = dataSource.getConnection()) {
            String usernameExists = translateInternal(connection, uuid.toString(), "username", SELECT_WITH_UUID);
            if (usernameExists == null) {
                preparedStatement = connection.prepareStatement(Strings.replaceWithOrder(INSERT_PLAYER_DATA, sqlTable.getTableName()));
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, username);
            } else {
                preparedStatement = connection.prepareStatement(Strings.replaceWithOrder(UPDATE_PLAYER_USERNAME, sqlTable.getTableName()));
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, uuid.toString());
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            TLogger.getGlobalLogger().error("Database error: " + e.toString());
        } finally {
            SQLExecutor.freeStatement(preparedStatement, null);
        }
    }

    public static void importLocal() {
        File worldFolder = new File(getDefaultWorldName());
        if (!worldFolder.exists()) {
            TLogger.getGlobalLogger().error("Invalid \"world\" folder");
            return;
        }

        File playerDataFolder = new File(worldFolder, "playerdata");
        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
            TLogger.getGlobalLogger().error("Invalid \"playerdata\" folder");
            return;
        }

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(32);

        AtomicInteger i = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();
        int size = playerDataFolder.listFiles().length;

        TLogger.getGlobalLogger().info("Start importing the local data...");
        for (File file : playerDataFolder.listFiles()) {
            if (fail.get() > 10) {
                TLogger.getGlobalLogger().info("The number of failures exceeds the threshold! import stopped..");
                break;
            }
            threadPool.submit(() -> {
                try {
                    String username = getUsernameInDatFile(file);
                    updateUsername(UUID.fromString(file.getName().split("\\.")[0]), username);
                    TLogger.getGlobalLogger().info("importing... " + username + "(" + i.getAndIncrement() + "/" + size + ")");
                } catch (Exception ignored) {
                    fail.getAndIncrement();
                }
            });
        }

        threadPool.shutdown();
    }

    public static JSONObject getPlayerDataInDatFile(File datFile) {
        Class<?> clazz = NMSUtils.getNMSClassSilent("NBTCompressedStreamTools");
        try (FileInputStream fileInputStream = new FileInputStream(datFile)) {
            Method a = clazz.getMethod("a", InputStream.class);
            Object nbtTagCompound = a.invoke(null, fileInputStream);
            return new JSONObject(nbtTagCompound.getClass().getMethod("toString").invoke(nbtTagCompound));
        } catch (Exception ignored) {
        }
        return new JSONObject();
    }

    // *********************************
    //
    //         Private Methods
    //
    // *********************************

    private static void createTable() {
        PreparedStatement preparedStatement = null;
        try (Connection connection = dataSource.getConnection()) {
            preparedStatement = connection.prepareStatement(sqlTable.createQuery());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            TLogger.getGlobalLogger().error("Database error: " + e.toString());
        } finally {
            SQLExecutor.freeStatement(preparedStatement, null);
        }
    }

    private static String getDefaultWorldName() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(new File("server.properties")));
        } catch (IOException ignored) {
        }
        return properties.getProperty("level-name", "world");
    }

    private static String getUsernameInDatFile(File datFile) {
        Class<?> clazz = NMSUtils.getNMSClassSilent("NBTCompressedStreamTools");
        try (FileInputStream fileInputStream = new FileInputStream(datFile)) {
            Method a = clazz.getMethod("a", InputStream.class);
            Object nbtTagCompound = a.invoke(null, fileInputStream);
            Method getCompound = nbtTagCompound.getClass().getMethod("getCompound", String.class);
            Object bukkit = getCompound.invoke(nbtTagCompound, "bukkit");
            Method getString = bukkit.getClass().getMethod("getString", String.class);
            return String.valueOf(getString.invoke(bukkit, "lastKnownName"));
        } catch (Exception ignored) {
        }
        return "null";
    }

    private static String translateInternal(Connection connection, String input, String output, String command) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(Strings.replaceWithOrder(command, sqlTable.getTableName()));
            preparedStatement.setString(1, input);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(output);
            }
        } catch (SQLException e) {
            TLogger.getGlobalLogger().error("Database error: " + e.toString());
        } finally {
            SQLExecutor.freeStatement(preparedStatement, resultSet);
        }
        return null;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static boolean isEnabled() {
        return enabled;
    }
}
