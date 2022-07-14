package io.izzel.taboolib.module.db.sql.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TabooLib
 * io.izzel.taboolib.module.db.sql.query.ConsumerResult
 *
 * @author bkm016
 * @since 2020/11/5 10:38 上午
 */
public interface Task {

    interface Function<T> {

        @Nullable
        T execute(@NotNull ResultSet resultSet) throws SQLException;
    }

    interface Consumer {

        void execute(@NotNull ResultSet resultSet) throws SQLException;
    }
}
