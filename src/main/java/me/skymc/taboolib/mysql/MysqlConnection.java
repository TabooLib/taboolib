package me.skymc.taboolib.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Deprecated
public class MysqlConnection {

    /**
     * Create by Bkm016
     * <p>
     * 2017-7-22 23:25:55
     */

    private Connection connection = null;

    private Statement statement = null;

    private Boolean isConnection = false;

    public MysqlConnection(String ip, String port, String table, String user, String pass) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System("载入 MYSQL 系统库成功");
        } catch (ClassNotFoundException e) {
            System("载入 MYSQL 系统库失败");
        }

        // TODO STATE THE URL AND CONNECTION
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + table + "?characterEncoding=utf-8";

        // TODO CONNECTION
        try {
            connection = DriverManager.getConnection(url, user, pass);
            statement = connection.createStatement();

            isConnection = true;
            System("连接 MYSQL 数据库成功");

            Executors.newFixedThreadPool(1).execute(() -> {
                while (isConnection) {
                    try {
                        if (connection.isClosed()) {
                            connection = DriverManager.getConnection(url, user, pass);
                            System("数据库连接关闭, 正在重新连接... [Connection Closed]");
                        }

                        Thread.sleep(30000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            System("连接 MYSQL 数据库失败 详细信息: " + e.getLocalizedMessage());
        }
    }

    public void closeConnection() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }

            isConnection = false;
            System("结束 MYSQL 连接成功");
        } catch (SQLException e) {
            System("结束 MYSQL 连接失败 详细信息: " + e.getLocalizedMessage());
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Boolean isConnection() {
        try {
            if (statement.isClosed()) {
                statement = null;
                statement = connection.createStatement();
                System("数据库连接关闭, 正在重新连接... [Statement Closed]");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isConnection;
    }

    public Statement getStatement() {
        return this.statement;
    }

    /**
     * Example: SQL_CreateTable("tablename", new String[] { "Player" });
     */
    public void SQL_CreateTable(String table, String[] list) {
        if (!isConnection()) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < list.length; i++) {
            if (i + 1 < list.length) {
                stringBuilder.append("`").append(checkString(list[i])).append("` varchar(255), ");
            } else {
                stringBuilder.append("`").append(checkString(list[i])).append("` varchar(255)");
            }
        }
        String url = "CREATE TABLE IF NOT EXISTS `" + table + "` ( " + stringBuilder + " )";

        try {
            getStatement().execute(url);
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
        }
    }

    /**
     * Example: SQL_SetValues("tablename", new String[] { "Player" }, new String[] { "BlackSKY" });
     */
    public void SQL_SetValues(String table, String[] list, String[] values) {
        if (!isConnection()) {
            return;
        }

        StringBuilder listbuilder = new StringBuilder();
        StringBuilder valuebuilder = new StringBuilder();

        for (int i = 0; i < list.length; i++) {
            if (i + 1 < list.length) {
                listbuilder.append("`").append(checkString(list[i])).append("`, ");
                valuebuilder.append("'").append(checkString(values[i])).append("', ");
            } else {
                listbuilder.append("`").append(checkString(list[i])).append("`");
                valuebuilder.append("'").append(checkString(values[i])).append("'");
            }
        }

        String url = "INSERT INTO `" + table + "` ( " + listbuilder + " ) VALUES ( " + valuebuilder + " )";
        try {
            getStatement().execute(url);
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
            for (int i = 0; i < e.getStackTrace().length && i < 5; i++) {
                String name = e.getStackTrace()[i].getClassName();

                System("(" + i + ")位置: " + name.substring(0, name.lastIndexOf(".")));
                System("     类名: " + e.getStackTrace()[i].getFileName().replaceAll(".java", ""));
                System("     行数: " + e.getStackTrace()[i].getLineNumber());
            }
        }
    }

    /**
     * Example: SQL_GetValue("tablename", "Player", "BlackSKY", "Value");
     */
    public String SQL_GetValue(String table, String line, String linevalue, String row) {
        if (!isConnection()) {
            return null;
        }

        String url = "SELECT * FROM " + checkString(table) + " WHERE `" + checkString(line) + "` = '" + checkString(linevalue) + "'";
        try {
            ResultSet resultSet = getStatement().executeQuery(url);
            while (resultSet.next()) {
                return resultSet.getString(row);
            }
            resultSet.close();
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
        }
        return null;
    }

    /**
     * Example: SQL_GetValues("tablename", "Player");
     */
    public List<String> SQL_GetValues(String table, String row) {
        if (!isConnection()) {
            return null;
        }

        List<String> list = new ArrayList<>();

        String url = "SELECT * FROM " + checkString(table);
        try {
            ResultSet resultSet = getStatement().executeQuery(url);
            while (resultSet.next()) {
                if (resultSet.getString(row) == null) {
                    continue;
                }
                list.add(resultSet.getString(row));
            }
            resultSet.close();
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
        }
        return list;
    }

    /**
     * Example: SQL_isExists("tablename", "Player", "BlackSKY");
     */
    public boolean SQL_isExists(String table, String row, String value) {
        if (!isConnection()) {
            return true;
        }

        String url = "SELECT * FROM " + checkString(table) + " WHERE `" + checkString(row) + "` = '" + checkString(value) + "'";
        try {
            ResultSet resultSet = getStatement().executeQuery(url);
            while (resultSet.next()) {
                return true;
            }
            resultSet.close();
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
        }
        return false;
    }

    /**
     * Example: SQL_UpdateValue("tablename", "Player", "BlackSKY", "Value", "10")
     */
    public void SQL_UpdateValue(String table, String line, String linevalue, String row, String value) {
        if (!isConnection()) {
            return;
        }

        String url = "UPDATE `" + checkString(table) + "` SET `" + checkString(row) + "` = '" + checkString(value) + "' WHERE `" + checkString(line) + "` = '" + checkString(linevalue) + "'";
        try {
            getStatement().execute(url);
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
        }
    }

    /**
     * Example: SQL_DeleteValue("tablename", "BlackSKY");
     */
    public void SQL_DeleteValue(String table, String line, String linevalue) {
        if (!isConnection()) {
            return;
        }

        String url = "DELETE FROM `" + checkString(table) + "` WHERE `" + checkString(line) + "` = '" + checkString(linevalue) + "'";
        try {
            getStatement().execute(url);
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
        }
    }

    /**
     * Example: SQL_ClearTable("tablename");
     * @deprecated 即将过期
     */
    @Deprecated
    public void SQL_ClearTable(String table) {
        if (!isConnection()) {
            return;
        }

        String url = "TRUNCATE TABLE `" + checkString(table) + "`";
        try {
            getStatement().execute(url);
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
        }
    }

    public void SQL_execute(String url) {
        if (!isConnection()) {
            return;
        }

        try {
            getStatement().execute(url);
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
        }
    }

    public ResultSet SQL_executeQuery(String url) {
        if (!isConnection()) {
            return null;
        }

        try {
            return getStatement().executeQuery(url);
        } catch (SQLException e) {
            System("执行 MYSQL 任务出错 详细信息: " + e.getLocalizedMessage());
            System("任务: " + url);
            return null;
        }
    }

    public void SQL_clearTable(String table) {
        SQL_execute("DELETE FROM " + checkString(table) + ";");
    }

    public void SQL_deleteTable(String table) {
        SQL_execute("DROP TABLE " + checkString(table) + ";");
    }

    private void System(String string) {
        System.out.println("[TabooLib - MYSQL] " + string);
    }

    private String checkString(String string) {
        return string.replace("`", "").replace("'", "").replace("\"", "");
    }

}
