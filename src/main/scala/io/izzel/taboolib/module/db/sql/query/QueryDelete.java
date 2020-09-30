package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2019-10-26 13:34
 */
public class QueryDelete {

    private String tableName;
    private final List<Where> where = Lists.newArrayList();

    public QueryDelete table(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public QueryDelete where(Where where) {
        this.where.add(where);
        return this;
    }

    public QueryDelete where(Where... where) {
        Collections.addAll(this.where, where);
        return this;
    }

    public void run(DataSource dataSource) {
        to(dataSource).run();
    }

    public RunnableUpdate to(DataSource dataSource) {
        return new RunnableUpdate(toQuery()).dataSource(dataSource).statement(s -> {
            int index = 1;
            for (Where w : where) {
                index = w.toStatement(s, index);
            }
        });
    }

    public String toQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("delete from ").append(tableName);
        builder.append(" ");
        if (!where.isEmpty()) {
            builder.append("where ");
            builder.append(where.stream().map(Where::toQuery).collect(Collectors.joining(" and ")));
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
