package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.module.db.sql.SQLTable;
import io.izzel.taboolib.util.Pair;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sky
 * @since 2019-10-26 13:34
 */
public class QueryInsert extends Query {

    private final List<Object> value = Lists.newArrayList();
    private final List<String> columns = Lists.newArrayList();
    private final List<Pair<String, Object>> update = Lists.newArrayList();

    public QueryInsert(SQLTable table) {
        super(table);
    }

    public QueryInsert value(Object... value) {
        Collections.addAll(this.value, value);
        return this;
    }

    public QueryInsert columns(String... columns) {
        Collections.addAll(this.columns, columns);
        return this;
    }

    @SafeVarargs
    public final QueryInsert onDuplicateKey(Pair<String, Object>... update) {
        Collections.addAll(this.update, update);
        return this;
    }

    public void run(DataSource dataSource) {
        to(dataSource).run();
    }

    public RunnableUpdate to(DataSource dataSource) {
        return new RunnableUpdate(toQuery()).dataSource(dataSource).statement(s -> {
            int index = 1;
            for (Object v : value) {
                s.setObject(index++, v);
            }
            for (Pair<String, Object> entry : update) {
                s.setObject(index++, entry.getValue());
            }
        });
    }

    @Override
    public String toQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into `").append(table.getTableName());
        builder.append("`");
        if (!columns.isEmpty()) {
            builder.append("(");
            builder.append(columns.stream().map(i -> "`" + i + "`").collect(Collectors.joining(", ")));
            builder.append(")");
        }
        builder.append("values (");
        if (!value.isEmpty()) {
            builder.append(value.stream().map(i -> "?").collect(Collectors.joining(", ")));
            builder.append(" ");
        }
        builder.append(")");
        if (!update.isEmpty()) {
            builder.append("ON DUPLICATE KEY UPDATE ");
            builder.append(update.stream().map(i -> "`" + i.getKey() + "` = ?").collect(Collectors.joining(", ")));
        }
        return builder.toString().trim();
    }
}
