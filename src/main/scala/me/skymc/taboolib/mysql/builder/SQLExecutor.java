package me.skymc.taboolib.mysql.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @Author sky
 * @Since 2018-06-22 16:38
 */
public class SQLExecutor {

    public static void freeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ignored) {
        }
    }

    public static void freeStatement(PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception ignored) {
        }
    }
}
