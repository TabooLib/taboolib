package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.sql.SQLTable;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author sky
 * @since 2019-10-26 13:34
 */
public class QueryDelete extends Query {

    private final List<Where> where = Lists.newArrayList();

    public QueryDelete(SQLTable table) {
        super(table);
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

    @Override
    public String toQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("delete from `").append(table.getTableName()).append("` ");
        if (!where.isEmpty()) {
            builder.append("where ");
            StringJoiner joiner = new StringJoiner(" and ");
            for (Where i : where) {
                joiner.add(i.toQuery(table.getTableName()));
            }
            builder.append(joiner.toString());
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
