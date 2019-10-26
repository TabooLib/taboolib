package io.izzel.taboolib.module.db.sql.query;

import com.google.common.collect.Lists;
import io.izzel.taboolib.util.KV;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2019-10-26 13:34
 */
public class QueryUpdate {

    private String tableName;
    private List<Where> where = Lists.newArrayList();
    private List<KV<String, Object>> set = Lists.newArrayList();

    public QueryUpdate table(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public QueryUpdate set(String row, Object value) {
        this.set.add(new KV<>(row, value));
        return this;
    }

    public QueryUpdate where(Where where) {
        this.where.add(where);
        return this;
    }

    public QueryUpdate where(Where... where) {
        Collections.addAll(this.where, where);
        return this;
    }

    public RunnableUpdate to(DataSource dataSource) {
        return new RunnableUpdate(toQuery()).dataSource(dataSource).statement(s -> {
            int index = 1;
            for (KV<String, Object> pair : set) {
                s.setObject(index++, pair.getValue());
            }
            for (Where w : where) {
                index = w.toStatement(s, index);
            }
        });
    }

    public String toQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("update ").append(tableName);
        builder.append(" ");
        if (!set.isEmpty()) {
            builder.append("set ");
            builder.append(set.stream().map(s -> s.getKey() + " = ?").collect(Collectors.joining(", ")));
            builder.append(" ");
        }
        if (!where.isEmpty()) {
            builder.append("where ");
            builder.append(where.stream().map(Where::toQuery).collect(Collectors.joining(" and ")));
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
