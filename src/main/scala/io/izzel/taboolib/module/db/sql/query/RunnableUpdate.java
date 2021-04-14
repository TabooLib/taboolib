package io.izzel.taboolib.module.db.sql.query;

import io.izzel.taboolib.module.locale.logger.TLogger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author sky
 * @since 2018-07-03 21:29
 */
public class RunnableUpdate {

    private final String query;
    private DataSource dataSource;
    private TaskStatement statement;
    private Consumer<Connection> connectionFinish;
    private Consumer<Statement> statementFinish;
    private Consumer<Integer> result;

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

    public RunnableUpdate connectionFinish(Consumer<Connection> consumer) {
        this.connectionFinish = consumer;
        return this;
    }

    public RunnableUpdate statementFinish(Consumer<Statement> consumer) {
        this.statementFinish = consumer;
        return this;
    }

    public RunnableUpdate result(Consumer<Integer> consumer) {
        this.result = consumer;
        return this;
    }

    public void run() {
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                if (statement != null) {
                    statement.execute(preparedStatement);
                }
                int i = preparedStatement.executeUpdate();
                if (result != null) {
                    result.accept(i);
                }
                if (statementFinish != null) {
                    statementFinish.accept(preparedStatement);
                }
                if (connectionFinish != null) {
                    connectionFinish.accept(connection);
                }
            } catch (Exception e) {
                TLogger.getGlobalLogger().error("An exception occurred in the database. (" + query + ")");
                TLogger.getGlobalLogger().error("Reason: " + e.toString());
                e.printStackTrace();
            }
        }
    }
}
