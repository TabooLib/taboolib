package io.izzel.taboolib.module.db.sql;

import com.google.common.base.Enums;
import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.IColumn;
import io.izzel.taboolib.module.db.sql.query.*;
import io.izzel.taboolib.util.Strings;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * SQL 数据表
 *
 * @Author sky
 * @Since 2018-05-14 19:07
 */
public class SQLTable {

    private final String tableName;
    private final List<IColumn> columns = Lists.newArrayList();

    /**
     * 创建数据表实例
     *
     * @param tableName 表名
     */
    public SQLTable(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 创建数据表实例
     *
     * @param tableName 表名
     * @param column    列
     */
    public SQLTable(String tableName, IColumn... column) {
        this.tableName = tableName;
        this.columns.addAll(Arrays.asList(column));
    }

    public SQLTable(String tableName, SQLColumn... column) {
        this.tableName = tableName;
        this.columns.addAll(Arrays.asList(column));
    }

    /**
     * 5.41 update
     * 用该方法创建 SQLTable 对象会默认带有 PRIMARY_KEY_ID
     */
    public static SQLTable create(String name) {
        return new SQLTable(name, SQLColumn.PRIMARY_KEY_ID);
    }

    /**
     * 添加列
     */
    public SQLTable column(IColumn column) {
        columns.add(column);
        return this;
    }

    /**
     * 5.38 update
     */
    public SQLTable column(IColumn... column) {
        columns.addAll(Arrays.asList(column));
        return this;
    }

    /**
     * 创建表
     *
     * @param dataSource 连接池对象
     */
    public void create(DataSource dataSource) {
        executeUpdate(createQuery()).dataSource(dataSource).run();
    }

    public QuerySelect select() {
        return new QuerySelect(this).row("*");
    }

    public QuerySelect select(String... row) {
        return new QuerySelect(this).row(row);
    }

    public QuerySelect select(Where... where) {
        return new QuerySelect(this).where(where);
    }

    public QueryUpdate update() {
        return new QueryUpdate(this);
    }

    public QueryUpdate update(Where... where) {
        return new QueryUpdate(this).where(where);
    }

    public QueryInsert insert() {
        return new QueryInsert(this);
    }

    public QueryInsert insert(Object... value) {
        return new QueryInsert(this).value(value);
    }

    public QueryDelete delete() {
        return new QueryDelete(this);
    }

    public QueryDelete delete(Where... where) {
        return new QueryDelete(this).where(where);
    }

    /**
     * 4.x version
     */
    public RunnableQuery executeQuery(String query) {
        return new RunnableQuery(query);
    }

    public RunnableUpdate executeUpdate(String query) {
        return new RunnableUpdate(query);
    }

    @Deprecated
    public String deleteQuery() {
        return Strings.replaceWithOrder("drop table if exists `{0}`", tableName);
    }

    @Deprecated
    public String cleanQuery() {
        return Strings.replaceWithOrder("delete from `{0}`", tableName);
    }

    @Deprecated
    public String truncateQuery() {
        return Strings.replaceWithOrder("truncate table `{0}`", tableName);
    }

    @Deprecated
    public RunnableQuery executeSelect() {
        return executeQuery(Strings.replaceWithOrder("select * from `{0}`", tableName));
    }

    @Deprecated
    public RunnableQuery executeSelect(String queryWhere) {
        return executeQuery(Strings.replaceWithOrder("select * from `{0}` where {1}", tableName, queryWhere));
    }

    @Deprecated
    public RunnableUpdate executeInsert(String queryValues) {
        return executeUpdate(Strings.replaceWithOrder("insert into `{0}` values({1})", tableName, queryValues));
    }

    @Deprecated
    public RunnableUpdate executeUpdate(String update, String where) {
        return executeUpdate(Strings.replaceWithOrder("update `{0}` set {1} where {2}", tableName, update, where));
    }

    @Deprecated
    public String createQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("create table if not exists `").append(tableName).append("` (");
        builder.append(columns.stream()
                .map(i -> i.convertToCommand().trim())
                .collect(Collectors.joining(", ")));

        // 5.41 更新，优化 SQL 类型下的建表命令
        {
            List<SQLColumn> uniqueKey = Lists.newArrayList();
            List<SQLColumn> normalKey = Lists.newArrayList();
            columns.stream().filter(i -> i instanceof SQLColumn).forEach(i -> {
                List<SQLColumnOption> columnOptions = ((SQLColumn) i).getColumnOptions();
                if (columnOptions.contains(SQLColumnOption.UNIQUE_KEY)) {
                    uniqueKey.add((SQLColumn) i);
                } else if (columnOptions.contains(SQLColumnOption.KEY)) {
                    normalKey.add((SQLColumn) i);
                }
            });
            if (uniqueKey.size() > 0) {
                builder.append(Strings.replaceWithOrder(", unique key `uk_{0}` ({1})",
                        uniqueKey.stream().map(SQLColumn::getColumnName)
                                .collect(Collectors.joining("_")),
                        uniqueKey.stream().map(i -> "`" + i.getColumnName() + "`" + (i.isDescendingIndex() ? " desc" : ""))
                                .collect(Collectors.joining(", "))
                ));
            }
            if (normalKey.size() > 0) {
                builder.append(Strings.replaceWithOrder(", key `idx_{0}` ({1})",
                        normalKey.stream().map(SQLColumn::getColumnName)
                                .collect(Collectors.joining("_")),
                        normalKey.stream().map(i -> "`" + i.getColumnName() + "`" + (i.isDescendingIndex() ? " desc" : ""))
                                .collect(Collectors.joining(", "))
                ));
            }
        }
        return builder.append(")").toString();
    }

    /**
     * 5.1 update
     */
    @Deprecated
    public SQLTable column(String... column) {
        for (String c : column) {
            if (c.equalsIgnoreCase("$primary_key_id") || c.equalsIgnoreCase("$id")) {
                column(SQLColumn.PRIMARY_KEY_ID);
            } else {
                String[] v = c.split(":");
                if (v.length == 2) {
                    column(new SQLColumn(Enums.getIfPresent(SQLColumnType.class, v[0].toUpperCase()).or(SQLColumnType.TEXT), v[1]));
                } else {
                    column(new SQLColumn(SQLColumnType.TEXT, "error_" + c));
                }
            }
        }
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public IColumn[] getColumns() {
        return columns.toArray(new IColumn[0]);
    }
}
