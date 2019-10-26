package io.izzel.taboolib.module.db.sql;

import com.google.common.base.Enums;
import io.izzel.taboolib.module.db.IColumn;
import io.izzel.taboolib.module.db.sql.query.*;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.Strings;

import javax.sql.DataSource;

/**
 * @Author sky
 * @Since 2018-05-14 19:07
 */
public class SQLTable {

    private String tableName;
    private IColumn[] columns;

    public SQLTable(String tableName) {
        this.tableName = tableName;
    }

    public SQLTable(String tableName, IColumn... column) {
        this.tableName = tableName;
        this.columns = column;
    }

    public SQLTable(String tableName, SQLColumn... column) {
        this.tableName = tableName;
        this.columns = column;
    }

    public SQLTable column(IColumn column) {
        columns = columns == null ? new IColumn[] {column} : ArrayUtil.arrayAppend(columns, column);
        return this;
    }

    /**
     * 5.1 update
     */
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

    public void create(DataSource dataSource) {
        executeUpdate(createQuery()).dataSource(dataSource).run();
    }

    public QuerySelect select() {
        return new QuerySelect().row("*").table(tableName);
    }

    public QuerySelect select(String... row) {
        return new QuerySelect().row(row).table(tableName);
    }

    public QuerySelect select(Where... where) {
        return new QuerySelect().where(where).table(tableName);
    }

    public QueryUpdate update() {
        return new QueryUpdate().table(tableName);
    }

    public QueryUpdate update(Where... where) {
        return new QueryUpdate().where(where).table(tableName);
    }

    public QueryInsert insert() {
        return new QueryInsert().table(tableName);
    }

    public QueryInsert insert(Object... value) {
        return new QueryInsert().table(tableName).value(value);
    }

    public QueryDelete delete() {
        return new QueryDelete().table(tableName);
    }

    public QueryDelete delete(Where... where) {
        return new QueryDelete().table(tableName).where(where);
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
        return executeQuery("select * from " + tableName);
    }

    @Deprecated
    public RunnableQuery executeSelect(String queryWhere) {
        return executeQuery("select * from " + tableName + " where " + queryWhere);
    }

    @Deprecated
    public RunnableUpdate executeInsert(String queryValues) {
        return executeUpdate("insert into " + tableName + " values(" + queryValues + ")");
    }

    @Deprecated
    public RunnableUpdate executeUpdate(String update, String where) {
        return executeUpdate("update " + tableName + " set " + update + " where " + where);
    }

    @Deprecated
    public String createQuery() {
        StringBuilder builder = new StringBuilder();
        java.util.Arrays.stream(columns).forEach(sqlColumn -> builder.append(sqlColumn.convertToCommand()).append(", "));
        return Strings.replaceWithOrder("create table if not exists `{0}` ({1})", tableName, builder.substring(0, builder.length() - 2));
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public String getTableName() {
        return tableName;
    }

    public IColumn[] getColumns() {
        return columns;
    }
}
