package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.sql.SQLTable;
import io.izzel.taboolib.util.Pair;
import io.izzel.taboolib.util.Strings;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author sky
 * @since 2019-10-26 13:34
 */
public class QueryInsert extends Query {

    private final List<String> columns = Lists.newArrayList();
    private final List<List<Object>> value = Lists.newArrayList();
    private final List<Pair<String, Object>> update = Lists.newArrayList();

    public QueryInsert(SQLTable table) {
        super(table);
    }

    public QueryInsert value(Object... value) {
        this.value.add(Lists.newArrayList(value));
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
            for (List<Object> value : value) {
                for (Object v : value) {
                    s.setObject(index++, v);
                }
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
            StringJoiner joiner = new StringJoiner(", ");
            for (String i : columns) {
                joiner.add(Strings.replaceWithOrder("`{0}`", i));
            }
            builder.append(joiner.toString());
            builder.append(")");
        }
        builder.append("values ");
        for (List<Object> value : value) {
            builder.append("(");
            StringJoiner joiner = new StringJoiner(", ");
            for (Object i : value) {
                joiner.add("?");
            }
            builder.append(joiner.toString());
            builder.append(" ");
            builder.append(") ");
        }
        if (!update.isEmpty()) {
            builder.append("ON DUPLICATE KEY UPDATE ");
            StringJoiner joiner = new StringJoiner(", ");
            for (Pair<String, Object> i : update) {
                joiner.add(Strings.replaceWithOrder("`{0}` = ?", i.getKey()));
            }
            builder.append(joiner.toString());
        }
        return builder.toString().trim();
    }
}
