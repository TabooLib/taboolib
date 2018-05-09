package me.skymc.taboolib.mysql.protect;

import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author sky
 */
public class MySQLConnection {

    private String url;
    private String user;
    private String port;
    private String password;
    private String database;
    private String connectionUrl;
    private Connection connection;
    private Plugin plugin;
    private boolean fallReconnection = true;
    private int recheck = 10;
    private Thread recheckThread;

    public MySQLConnection(String url, String user, String port, String password, String database) {
        this(url, user, port, password, database, 10, Main.getInst());
    }

    public MySQLConnection(String url, String user, String port, String password, String database, int recheck, Plugin plugin) {
        // 检查驱动
        if (!loadDriverMySQL()) {
            TLocale.Logger.error("MYSQL-CONNECTION.FALL-NOTFOUND-DRIVE");
            return;
        }

        // 设置信息
        this.plugin = plugin;
        this.recheck = recheck;

        // 设置数据
        this.url = url == null ? "localhost" : url;
        this.user = user == null ? "root" : user;
        this.port = port == null ? "3306" : port;
        this.password = password == null ? "" : password;
        this.database = database == null ? "test" : database;
        this.connectionUrl = Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}?characterEncoding=utf-8&useSSL=false", this.url, this.port, this.database);

        // 连接数据库
        connect();

        // 断线检测
        recheckThread = new Thread(() -> {
            while (!Main.isDisable()) {
                try {
                    Thread.sleep(getReCheckSeconds() * 1000);

                    if (connection == null) {
                        TLocale.Logger.error("MYSQL-CONNECTION.FALL-NOTFOUND-CONNECTION", plugin.getName());
                    } else {
                        isExists("taboolib");
                    }
                } catch (Exception e) {
                    TLocale.Logger.error("MYSQL-CONNECTION.FALL-COMMAND-NORMAL", e.toString());
                }
            }
        });

        // 启动检测
        if (isConnection()) {
            recheckThread.start();
            TLocale.Logger.info("MYSQL-CONNECTION.SUCCESS-REGISTERED-LISTENER");
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public Connection getConnection() {
        return connection;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isFallReconnection() {
        return fallReconnection;
    }

    public void setFallReconnection(boolean fallReconnection) {
        this.fallReconnection = fallReconnection;
    }

    public int getRecheck() {
        return recheck;
    }

    public Thread getRecheckThread() {
        return recheckThread;
    }

    public int getReCheckSeconds() {
        return recheck;
    }

    public void setReCheckSeconds(int s) {
        this.recheck = s;
    }

    public boolean isConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    public void closeConnection() {
        try {
            connection.close();
        } catch (Exception ignored) {
        }
        try {
            recheckThread.stop();
        } catch (Exception ignored) {
        }
    }

    public boolean deleteTable(String name) {
        return execute("drop table if exists " + name);
    }

    /**
     * 2018年1月17日 新增, TabooLib 版本 3.25
     */
    public boolean truncateTable(String name) {
        return execute("truncate table " + name);
    }

    public boolean clearTable(String name) {
        return execute("delete from " + name);
    }

    public boolean renameTable(String name, String newName) {
        return execute(Strings.replaceWithOrder("rename table `{0}` to `{1}`", name, newName));
    }

    public boolean deleteColumn(String name, String column) {
        return execute(Strings.replaceWithOrder("alter table `{0}` drop `{1}`", name, column));
    }

    public void addColumn(String name, Column... columns) {
        Arrays.stream(columns).map(column -> Strings.replaceWithOrder("alter table {0} add {1}", name, column.toString())).forEach(this::execute);
    }

    public boolean addColumn(String name, String column) {
        if (!column.contains("/")) {
            return execute(Strings.replaceWithOrder("alter table {0} add `{1}` text", name, column));
        }
        return execute(Strings.replaceWithOrder("alter table {0} add `{1}` {2}", name, column.split("/")[0], column.split("/")[1]));
    }

    public boolean editColumn(String name, String oldColumn, Column newColumn) {
        return execute(Strings.replaceWithOrder("alter table {0} change `{1}` {2}", name, oldColumn, newColumn.toString()));
    }

    public boolean editColumn(String name, String oldColumn, String newColumn) {
        if (!newColumn.contains("/")) {
            return execute(Strings.replaceWithOrder("alter table {0} change `{1}` `{2}` text", name, oldColumn, newColumn));
        }
        return execute(Strings.replaceWithOrder("alter table {0} change `{1}` `{2}` {3}", name, oldColumn, newColumn.split("/")[0], newColumn.split("/")[1]));
    }

    /**
     * 删除数据
     *
     * @param name        名称
     * @param column      参考列
     * @param columnValue 参考值
     * @return boolean
     */
    public boolean deleteValue(String name, String column, Object columnValue) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("delete from `{0}` where `{1}` = ?", name, column));
            preparedStatement.setObject(1, columnValue);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(null, preparedStatement);
        }
        return false;
    }

    /**
     * 写入数据
     *
     * @param name        名称
     * @param column      参考列
     * @param columnValue 参考值
     * @param valueColumn 数据列
     * @param value       数据值
     * @return boolean
     */
    public boolean setValue(String name, String column, Object columnValue, String valueColumn, Object value) {
        return setValue(name, column, columnValue, valueColumn, value, false);
    }

    /**
     * 写入数据
     *
     * @param name        名称
     * @param column      参考列
     * @param columnValue 参考值
     * @param valueColumn 数据列
     * @param value       数据值
     * @param append      是否追加（数据列类型必须为数字）
     * @return boolean
     */
    public boolean setValue(String name, String column, Object columnValue, String valueColumn, Object value, boolean append) {
        PreparedStatement preparedStatement = null;
        try {
            if (append) {
                preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("update `{0}` set `{1}` = `{2}` + ? where `{3}` = ?", name, valueColumn, valueColumn, column));
            } else {
                preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("update `{0}` set `{1}` = ? where `{2}` = ?", name, valueColumn, column));
            }
            preparedStatement.setObject(1, value);
            preparedStatement.setObject(2, columnValue);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(null, preparedStatement);
        }
        return false;
    }

    /**
     * 插入数据
     *
     * @param name   名称
     * @param values 值
     * @return boolean
     */
    public boolean intoValue(String name, Object... values) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(values).map(value -> "?, ").forEach(sb::append);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("insert into `{0}` values(null, {1})", name, sb.substring(0, sb.length() - 2)));
            for (int i = 0; i < values.length; i++) {
                preparedStatement.setObject(i + 1, values[i]);
            }
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(null, preparedStatement);
        }
        return false;
    }

    /**
     * 创建数据表
     *
     * @param name    名称
     * @param columns 列表
     * @return boolean
     */
    public boolean createTable(String name, Column... columns) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(columns).forEach(column -> sb.append(column.toString()).append(", "));
        return execute(Strings.replaceWithOrder("create table if not exists {0} (id int(1) not null primary key auto_increment, {1})", name, sb.substring(0, sb.length() - 2)));
    }

    /**
     * 创建数据表
     *
     * @param name    名称
     * @param columns 列表
     * @return boolean
     */
    public boolean createTable(String name, String... columns) {
        StringBuilder sb = new StringBuilder();
        for (String column : columns) {
            if (!column.contains("/")) {
                sb.append("`").append(column).append("` text, ");
            } else {
                sb.append("`").append(column.split("/")[0]).append("` ").append(column.split("/")[1]).append(", ");
            }
        }
        return execute(Strings.replaceWithOrder("create table if not exists {0} (id int(1) not null primary key auto_increment, {1})", name, sb.substring(0, sb.length() - 2)));
    }

    /**
     * 检查数据表是否存在
     *
     * @param name 名称
     * @return boolean
     */
    public boolean isExists(String name) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("select table_name FROM information_schema.TABLES where table_name = ?");
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        return false;
    }

    /**
     * 检查数据是否存在
     *
     * @param name        名称
     * @param column      列表名
     * @param columnValue 列表值
     * @return boolean
     */
    public boolean isExists(String name, String column, Object columnValue) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` where `{1}` = ?", name, column));
            preparedStatement.setObject(1, columnValue);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        return false;
    }

    /**
     * 获取所有列表名称（不含主键）
     *
     * @param name 名称
     * @return {@link List}
     */
    public List<String> getColumns(String name) {
        return getColumns(name, false);
    }

    /**
     * 获取所有列表名称
     *
     * @param name    名称
     * @param primary 是否获取主键
     * @return {@link List}
     */
    public List<String> getColumns(String name, boolean primary) {
        List<String> list = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("select column_name from information_schema.COLUMNS where table_name = ?");
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        // 是否获取主键
        if (!primary) {
            list.remove("id");
        }
        return list;
    }

    /**
     * 获取单项数据
     *
     * @param name        名称
     * @param column      参考列
     * @param columnValue 参考值
     * @param valueColumn 数据列
     * @return Object
     */
    public Object getValue(String name, String column, Object columnValue, String valueColumn) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` where `{1}` = ? limit 1", name, column));
            preparedStatement.setObject(1, columnValue);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getObject(valueColumn);
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        return null;
    }

    /**
     * 获取单项数据（根据主键倒叙排列后的最后一项）
     *
     * @param name        名称
     * @param column      参考列
     * @param columnValue 参考值
     * @param valueColumn 数据列
     * @return Object
     */
    public Object getValueLast(String name, String column, Object columnValue, String valueColumn) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` where `{1}` = ? order by id desc limit 1", name, column));
            preparedStatement.setObject(1, columnValue);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getObject(valueColumn);
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        return null;
    }

    /**
     * 获取多项数据（根据主键倒叙排列后的最后一项）
     *
     * @param name        名称
     * @param column      参考列
     * @param columnValue 参考值
     * @param valueColumn 数据列
     * @return {@link HashMap}
     */
    public HashMap<String, Object> getValueLast(String name, String column, Object columnValue, String... valueColumn) {
        HashMap<String, Object> map = new HashMap<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` where `{1}` = ? order by id desc limit 1", name, column));
            preparedStatement.setObject(1, columnValue);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                for (String _column : valueColumn) {
                    map.put(_column, resultSet.getObject(_column));
                }
                break;
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        return map;
    }

    /**
     * 获取多项数据（单项多列）
     *
     * @param name        名称
     * @param column      参考列
     * @param columnValue 参考值
     * @param valueColumn 数据列
     * @return {@link HashMap}
     */
    public HashMap<String, Object> getValue(String name, String column, Object columnValue, String... valueColumn) {
        HashMap<String, Object> map = new HashMap<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` where `{1}` = ? limit 1", name, column));
            preparedStatement.setObject(1, columnValue);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                for (String _column : valueColumn) {
                    map.put(_column, resultSet.getObject(_column));
                }
                break;
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        return map;
    }

    /**
     * 获取多项数据（单列多列）
     *
     * @param name   名称
     * @param column 参考列
     * @param size   获取数量（-1 为无限制）
     * @return {@link List}
     */
    public List<Object> getValues(String name, String column, int size) {
        return getValues(name, column, size, false);
    }

    /**
     * 获取多项数据（单列多列）
     *
     * @param name   名称
     * @param column 参考列
     * @param size   获取数量（-1 位无限制）
     * @param desc   是否倒序
     * @return {@link List}
     */
    public List<Object> getValues(String name, String column, int size, boolean desc) {
        List<Object> list = new LinkedList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            if (desc) {
                preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` order by `{1}` desc {2}", name, column, size < 0 ? "" : " limit " + size));
            } else {
                preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` order by `{1}` {2}", name, column, size < 0 ? "" : " limit " + size));
            }
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getObject(column));
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        return list;
    }

    /**
     * 获取多线数据（多项多列）
     *
     * @param name        名称
     * @param sortColumn  参考列（该列类型必须为数字）
     * @param size        获取数量（-1 为无限制）
     * @param valueColumn 获取数据列
     * @return {@link LinkedList}
     */
    public LinkedList<HashMap<String, Object>> getValues(String name, String sortColumn, int size, String... valueColumn) {
        return getValues(name, sortColumn, size, false, valueColumn);
    }

    /**
     * 获取多项数据（多项多列）
     *
     * @param name        名称
     * @param sortColumn  参考列（该列类型必须为数字）
     * @param size        获取数量（-1 为无限制）
     * @param desc        是否倒序
     * @param valueColumn 获取数据列
     * @return {@link LinkedList}
     */
    public LinkedList<HashMap<String, Object>> getValues(String name, String sortColumn, int size, boolean desc, String... valueColumn) {
        LinkedList<HashMap<String, Object>> list = new LinkedList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            if (desc) {
                preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` order by `{1}` desc{2}", name, sortColumn, size < 0 ? "" : " limit " + size));
            } else {
                preparedStatement = connection.prepareStatement(Strings.replaceWithOrder("select * from `{0}` order by `{1}`{2}", name, sortColumn, size < 0 ? "" : " limit " + size));
            }
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                HashMap<String, Object> map = new HashMap<>();
                for (String _column : valueColumn) {
                    map.put(_column, resultSet.getObject(_column));
                }
                list.add(map);
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            freeResult(resultSet, preparedStatement);
        }
        return list;
    }

    public boolean execute(String sql) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            printExceptionDetail(e);
            return false;
        } finally {
            freeResult(null, preparedStatement);
        }
    }

    public boolean connect() {
        TLocale.Logger.info("MYSQL-CONNECTION.NOTIFY-CONNECTING", connectionUrl);
        try {
            long time = System.currentTimeMillis();
            connection = DriverManager.getConnection(connectionUrl, this.user, this.password);
            TLocale.Logger.info("MYSQL-CONNECTION.NOTIFY-CONNECTED", String.valueOf(System.currentTimeMillis() - time));
            return true;
        } catch (SQLException e) {
            printExceptionDetail(e);
            return false;
        }
    }

    public void print(String message) {
        System.out.println("[TabooLib - MySQL] " + message);
    }

    private void printException(Exception e) {
        TLocale.Logger.error("MYSQL-CONNECTION.FALL-COMMAND-NORMAL", e.toString());
        reconnection(e);
    }

    private void printExceptionDetail(SQLException e) {
        TLocale.Logger.error("MYSQL-CONNECTION.FALL-COMMAND-DETAIL", String.valueOf(e.getErrorCode()), e.toString());
        reconnection(e);
    }

    private void reconnection(Exception e) {
        if (fallReconnection && e.getMessage().contains("closed")) {
            connect();
        }
    }

    private boolean loadDriverMySQL() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void freeResult(ResultSet resultSet, PreparedStatement preparedStatement) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception ignored) {
        }
    }

    public enum ColumnInteger {

        TINYINT, SMALLINT, MEDIUMINT, INT, BIGINT
    }

    public enum ColumnFloat {

        FLOAT, DOUBLE
    }

    public enum ColumnChar {

        CHAR, VARCHAR
    }

    public enum ColumnString {

        TINYTEXT, TEXT, MEDIUMTEXT, LONGTEXT
    }

    public static class Column {

        private String name;
        private Object type;
        private int a;
        private int b;

        public Column(String name) {
            this.name = name;
            this.type = ColumnString.TEXT;
        }

        public Column(String name, ColumnInteger type) {
            this(name);
            this.type = type;
            this.a = 12;
        }

        public Column(String name, ColumnInteger type, int m) {
            this(name);
            this.type = type;
            this.a = m;
        }

        public Column(String name, ColumnFloat type, int m, int d) {
            this(name);
            this.type = type;
            this.a = m;
            this.b = d;
        }

        public Column(String name, ColumnChar type, int n) {
            this(name);
            this.type = type;
            this.a = n;
        }

        public Column(String name, ColumnString type) {
            this(name);
            this.type = type;
        }

        @Override
        public String toString() {
            if (type instanceof ColumnInteger || type instanceof ColumnChar) {
                return Strings.replaceWithOrder("`{0}` {1}({2})", name, type.toString().toLowerCase(), a);
            } else if (type instanceof ColumnFloat) {
                return Strings.replaceWithOrder("`{0}` {1}({2},{3})", name, type.toString().toLowerCase(), a, b);
            } else {
                return Strings.replaceWithOrder("`{0}` {1}", name, type.toString().toLowerCase());
            }
        }
    }
}
