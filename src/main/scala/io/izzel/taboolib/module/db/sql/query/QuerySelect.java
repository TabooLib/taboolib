package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.sql.SQLTable;
import io.izzel.taboolib.util.ArrayUtil;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2019-10-26 13:34
 */
public class QuerySelect extends Query {

    private List<String> rowName = Lists.newArrayList();
    private String distinct;
    private final List<Where> where = Lists.newArrayList();
    private final List<Order> order = Lists.newArrayList();
    private int limit = -1;

    public QuerySelect(SQLTable table) {
        super(table);
    }

    public QuerySelect row(String... row) {
        this.rowName = ArrayUtil.asList(row);
        return this;
    }

    public QuerySelect distinct(String row) {
        this.distinct = row;
        return this;
    }

    public QuerySelect where(Where where) {
        this.where.add(where);
        return this;
    }

    public QuerySelect where(Where... where) {
        Collections.addAll(this.where, where);
        return this;
    }

    public QuerySelect order(Order order) {
        this.order.add(order);
        return this;
    }

    public QuerySelect limit(int limit) {
        this.limit = limit;
        return this;
    }

    public boolean find(DataSource dataSource) {
        return to(dataSource).find();
    }

    public RunnableQuery to(DataSource dataSource) {
        return new RunnableQuery(toQuery()).dataSource(dataSource).statement(s -> {
            int index = 1;
            for (Where w : where) {
                index = w.toStatement(s, index);
            }
        });
    }

    @Override
    public String toQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("select");
        builder.append(" ");
        if (!rowName.isEmpty()) {
            builder.append(String.join(", ", rowName));
        } else if (distinct != null) {
            builder.append("distinct ").append(distinct);
        } else {
            builder.append("*");
        }
        builder.append(" ");
        builder.append("from ").append(table.getTableName());
        builder.append(" ");
        if (!where.isEmpty()) {
            builder.append("where ");
            builder.append(where.stream().map(Where::toQuery).collect(Collectors.joining(" and ")));
            builder.append(" ");
        }
        if (!order.isEmpty()) {
            builder.append("order by ");
            builder.append(order.stream().map(Order::toQuery).collect(Collectors.joining(", ")));
            builder.append(" ");
        }
        if (limit > -1) {
            builder.append("limit ").append(limit);
        }
        return builder.toString().trim();
    }
}
