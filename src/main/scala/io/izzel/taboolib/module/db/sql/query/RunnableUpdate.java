package io.izzel.taboolib.module.db.sql.query;

import io.izzel.taboolib.module.locale.logger.TLogger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author sky
 * @since 2018-07-03 21:29
 */
public class RunnableUpdate {

    private final String query;
    private DataSource dataSource;
    private TaskStatement statement;

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

    public void run() {
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                if (statement != null) {
                    statement.execute(preparedStatement);
                }
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                TLogger.getGlobalLogger().error("An exception occurred in the database. (" + query + ")");
                TLogger.getGlobalLogger().error("Reason: " + e.toString());
                e.printStackTrace();
            }
        }
    }
}
