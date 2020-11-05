package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author sky
 * @Since 2018-07-03 21:29
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class RunnableQuery {

    private final String query;
    private DataSource dataSource;
    private TaskStatement statement;
    private TaskResult result;
    private TaskResult resultNext;
    private TaskResult resultAutoNext;

    public RunnableQuery(String query) {
        this.query = query;
    }

    public RunnableQuery dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public RunnableQuery statement(TaskStatement statement) {
        this.statement = statement;
        return this;
    }

    /**
     * 是否存在结果
     */
    public boolean find() {
        this.resultNext = r -> true;
        return run(false, false);
    }

    /**
     * 获取首个结果
     */
    @Nullable
    public <T> T first(Task.Function<T> task) {
        AtomicReference<T> result = new AtomicReference<>();
        resultNext(r -> {
            result.set(task.execute(r));
            return null;
        }).run();
        return result.get();
    }

    /**
     * 获取首个结果，检测 NULL 并返回默认值
     */
    @Nullable
    @Contract("_, !null -> !null")
    public <T> T firstOrElse(Task.Function<T> task, @Nullable T def) {
        T result = first(task);
        return result == null ? def : result;
    }

    /**
     * 获取所有结果，并转换
     */
    @NotNull
    public <T> List<T> map(Task.Function<T> task) {
        List<T> list = Lists.newArrayList();
        resultAutoNext(r -> list.add(task.execute(r))).run();
        return list;
    }

    /**
     * 获取所有结果，并遍历
     */
    public void forEach(Task.Consumer task) {
        resultAutoNext(r -> {
            task.execute(r);
            return null;
        }).run();
    }

    @Deprecated
    public RunnableQuery result(TaskResult result) {
        this.result = result;
        return this;
    }

    @Deprecated
    public RunnableQuery resultNext(TaskResult result) {
        this.resultNext = result;
        return this;
    }

    @Deprecated
    public RunnableQuery resultAutoNext(TaskResult result) {
        this.resultAutoNext = result;
        return this;
    }

    @Deprecated
    public <T> T run(Object def, T translate) {
        Object object = run(def);
        return object == null ? def == null ? null : (T) def : (T) object;
    }

    @Deprecated
    public <T> T run(Object def, Class<? extends T> translate) {
        Object object = run(def);
        return object == null ? def == null ? null : (T) def : (T) object;
    }

    @Deprecated
    public Object run() {
        return run(null);
    }

    @Deprecated
    public Object run(Object def) {
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                if (statement != null) {
                    statement.execute(preparedStatement);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return getResult(resultSet);
                }
            } catch (Exception e) {
                TLogger.getGlobalLogger().error("An exception occurred in the database. (" + query + ")");
                TLogger.getGlobalLogger().error("Reason: " + e.toString());
                e.printStackTrace();
            }
        }
        return def;
    }

    private Object getResult(ResultSet resultSet) throws SQLException {
        if (resultNext != null && resultSet.next()) {
            return resultNext.execute(resultSet);
        } else if (result != null) {
            return result.execute(resultSet);
        } else if (resultAutoNext != null) {
            Object result = null;
            while (resultSet.next()) {
                result = resultAutoNext.execute(resultSet);
            }
            return result;
        } else {
            return null;
        }
    }
}