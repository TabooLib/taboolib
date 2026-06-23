package taboolib.module.database;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseTest {

    @TempDir
    Path tempDir;

    @Test
    public void createDataSourceWithoutConfigShouldApplySQLiteConnectionParameters() throws Exception {
        HostSQLite host = new HostSQLite(tempDir.resolve("database.db").toFile());

        try (HikariDataSource dataSource = (HikariDataSource) Database.INSTANCE.createDataSourceWithoutConfig(host)) {
            try (Connection connection = dataSource.getConnection()) {
                assertEquals("wal", readPragma(connection, "journal_mode"));
                assertEquals("5000", readPragma(connection, "busy_timeout"));
                assertEquals("1", readPragma(connection, "synchronous"));
            }
        }
    }

    @Test
    public void createDataSourceWithoutConfigShouldWaitForConcurrentSQLiteWrite() throws Exception {
        HostSQLite host = new HostSQLite(tempDir.resolve("concurrent.db").toFile());
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try (HikariDataSource dataSource = (HikariDataSource) Database.INSTANCE.createDataSourceWithoutConfig(host)) {
            try (Connection connection = dataSource.getConnection()) {
                execute(connection, "CREATE TABLE test_data (id INTEGER PRIMARY KEY AUTOINCREMENT, value TEXT)");
            }

            try (Connection first = dataSource.getConnection()) {
                first.setAutoCommit(false);
                execute(first, "INSERT INTO test_data (value) VALUES ('first')");

                Future<?> concurrentWrite = executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try (Connection second = dataSource.getConnection()) {
                            execute(second, "INSERT INTO test_data (value) VALUES ('second')");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                Thread.sleep(250);
                first.commit();
                concurrentWrite.get(2, TimeUnit.SECONDS);
            }

            try (Connection connection = dataSource.getConnection()) {
                assertEquals("2", readSql(connection, "SELECT COUNT(*) FROM test_data"));
            }
        } finally {
            executorService.shutdownNow();
        }
    }

    String readPragma(Connection connection, String name) throws Exception {
        return readSql(connection, "PRAGMA " + name);
    }

    String readSql(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                resultSet.next();
                return resultSet.getString(1);
            }
        }
    }

    void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
