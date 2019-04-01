package me.skymc.taboolib.mysql.builder.query;

import com.ilummc.tlib.logger.TLogger;
import me.skymc.taboolib.mysql.builder.SQLExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * F
 *
 * @Author sky
 * @Since 2018-07-03 21:29
 */
public class RunnableUpdate {

    private DataSource dataSource;
    private TaskStatement statement;
    private Connection connection;
    private boolean autoClose;
    private String query;

    public RunnableUpdate(String query) {
        this.query = query;
    }

    public RunnableUpdate dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public RunnableUpdate statement(TaskStatement task) {
        this.statement = task;
        return this;
    }

    public RunnableUpdate connection(Connection connection) {
        return connection(connection, false);
    }

    public RunnableUpdate connection(Connection connection, boolean autoClose) {
        this.connection = connection;
        this.autoClose = autoClose;
        return this;
    }

    public void run() {
        PreparedStatement preparedStatement = null;
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                preparedStatement = connection.prepareStatement(query);
                if (statement != null) {
                    statement.execute(preparedStatement);
                }
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                printException(e);
            } finally {
                SQLExecutor.freeStatement(preparedStatement, null);
            }
        } else if (connection != null) {
            try {
                preparedStatement = connection.prepareStatement(query);
                if (statement != null) {
                    statement.execute(preparedStatement);
                }
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                printException(e);
            } finally {
                SQLExecutor.freeStatement(preparedStatement, null);
                if (autoClose) {
                    SQLExecutor.freeConnection(connection);
                }
            }
        }
    }

    private void printException(Exception e) {
        TLogger.getGlobalLogger().error("An exception occurred in the database. (" + query + ")");
        TLogger.getGlobalLogger().error("Reason: " + e.toString());
        e.printStackTrace();
    }
}
