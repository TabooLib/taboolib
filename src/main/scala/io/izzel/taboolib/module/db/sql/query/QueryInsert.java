package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.sql.SQLTable;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sky
 * @since 2019-10-26 13:34
 */
public class QueryInsert extends Query {

    private final List<Object> value = Lists.newArrayList();

    public QueryInsert(SQLTable table) {
        super(table);
    }

    public QueryInsert value(Object... value) {
        Collections.addAll(this.value, value);
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
        });
    }

    @Override
    public String toQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(table.getTableName());
        builder.append(" ");
        builder.append("values (");
        if (!value.isEmpty()) {
            builder.append(value.stream().map(i -> "?").collect(Collectors.joining(", ")));
            builder.append(" ");
        }
        return builder.append(")").toString().trim();
    }
}
