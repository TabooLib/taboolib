package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.sql.SQLTable;
import io.izzel.taboolib.util.Strings;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author sky
 * @since 2019-10-26 13:34
 */
public class QuerySelect extends Query {

    private List<String> rowName = Lists.newArrayList();
    private String distinct;
    private final List<Where> where = Lists.newArrayList();
    private final List<Order> order = Lists.newArrayList();
    private final List<Join> join = Lists.newArrayList();
    private int limit = -1;

    public QuerySelect(SQLTable table) {
        super(table);
    }

    public QuerySelect row(String... row) {
        this.rowName = Lists.newArrayList(row);
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

    public QuerySelect innerJoin(SQLTable table, JoinWhere... where) {
        this.join.add(new Join(JoinType.INNER, table.getTableName(), Lists.newArrayList(where)));
        return this;
    }

    public QuerySelect innerJoin(String table, JoinWhere... where) {
        this.join.add(new Join(JoinType.INNER, table, Lists.newArrayList(where)));
        return this;
    }

    public QuerySelect leftJoin(SQLTable table, JoinWhere... where) {
        this.join.add(new Join(JoinType.LEFT, table.getTableName(), Lists.newArrayList(where)));
        return this;
    }

    public QuerySelect leftJoin(String table, JoinWhere... where) {
        this.join.add(new Join(JoinType.LEFT, table, Lists.newArrayList(where)));
        return this;
    }

    public QuerySelect rightJoin(SQLTable table, JoinWhere... where) {
        this.join.add(new Join(JoinType.RIGHT, table.getTableName(), Lists.newArrayList(where)));
        return this;
    }

    public QuerySelect rightJoin(String table, JoinWhere... where) {
        this.join.add(new Join(JoinType.RIGHT, table, Lists.newArrayList(where)));
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

    public String toSelect(String row) {
        return Strings.replaceWithOrder("`{0}.{1}`", table.getTableName(), row);
    }

    @Override
    public String toQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("select");
        builder.append(" ");
        if (!rowName.isEmpty()) {
            StringJoiner joiner = new StringJoiner(", ");
            for (String row : rowName) {
                joiner.add(toSelect(row));
            }
            builder.append(joiner.toString());
        } else if (distinct != null) {
            builder.append("distinct ").append(distinct);
        } else {
            builder.append("*");
        }
        builder.append(" ");
        builder.append("from `").append(table.getTableName());
        builder.append("` ");
        if (!join.isEmpty()) {
            StringJoiner joiner = new StringJoiner(" ");
            for (Join join : join) {
                joiner.add(join.toQuery());
            }
            builder.append(joiner.toString());
            builder.append(" ");
        }
        if (!where.isEmpty()) {
            builder.append("where ");
            StringJoiner joiner = new StringJoiner(" and ");
            for (Where where : where) {
                joiner.add(where.toQuery(table.getTableName()));
            }
            builder.append(joiner.toString());
            builder.append(" ");
        }
        if (!order.isEmpty()) {
            builder.append("order by ");
            StringJoiner joiner = new StringJoiner(", ");
            for (Order order : order) {
                joiner.add(order.toQuery(table.getTableName()));
            }
            builder.append(joiner.toString());
            builder.append(" ");
        }
        if (limit > -1) {
            builder.append("limit ").append(limit);
        }
        return builder.toString().trim();
    }
}
