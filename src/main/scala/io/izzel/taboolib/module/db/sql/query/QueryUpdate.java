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
public class QueryUpdate extends Query {

    private final List<Where> where = Lists.newArrayList();
    private final List<Pair<String, Object>> set = Lists.newArrayList();
    private final List<Object> insertIfAbsent = Lists.newArrayList();

    public QueryUpdate(SQLTable table) {
        super(table);
    }

    public QueryUpdate set(String row, Object value) {
        this.set.add(new Pair<>(row, value));
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

    /**
     * 5.38 update
     * 数据不存在则跳过 update 执行 insert 语句。
     *
     * @param value 内容
     * @return {@link QueryUpdate}
     */
    public QueryUpdate insertIfAbsent(Object... value) {
        Collections.addAll(this.insertIfAbsent, value);
        return this;
    }

    public void run(DataSource dataSource) {
        this.to(dataSource).run();
    }

    public RunnableUpdate to(DataSource dataSource) {
        if (!insertIfAbsent.isEmpty() && !table.select(where.toArray(new Where[0])).limit(1).find(dataSource)) {
            return table.insert(insertIfAbsent.toArray(new Object[0])).to(dataSource);
        }
        return new RunnableUpdate(toQuery()).dataSource(dataSource).statement(s -> {
            int index = 1;
            for (Pair<String, Object> pair : set) {
                s.setObject(index++, pair.getValue());
            }
            for (Where w : where) {
                index = w.toStatement(s, index);
            }
        });
    }

    public String toQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("update `").append(table.getTableName());
        builder.append("` ");
        if (!set.isEmpty()) {
            builder.append("set ");
            StringJoiner joiner = new StringJoiner(", ");
            for (Pair<String, Object> s : set) {
                joiner.add(Strings.replaceWithOrder("`{0}` = ?", s.getKey()));
            }
            builder.append(joiner.toString());
            builder.append(" ");
        }
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
