package me.skymc.taboolib.mysql.builder;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.mysql.IColumn;
import me.skymc.taboolib.mysql.builder.query.RunnableQuery;
import me.skymc.taboolib.mysql.builder.query.RunnableUpdate;
import me.skymc.taboolib.string.ArrayUtils;

import java.util.Arrays;

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
        columns = columns == null ? new IColumn[] {column} : ArrayUtils.arrayAppend(columns, column);
        return this;
    }

    @Deprecated
    public SQLTable addColumn(SQLColumn sqlColumn) {
        columns = columns == null ? new SQLColumn[] {sqlColumn} : ArrayUtils.arrayAppend(columns, sqlColumn);
        return this;
    }

    public String createQuery() {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(columns).forEach(sqlColumn -> builder.append(sqlColumn.convertToCommand()).append(", "));
        return Strings.replaceWithOrder("create table if not exists `{0}` ({1})", tableName, builder.substring(0, builder.length() - 2));
    }

    public String deleteQuery() {
        return Strings.replaceWithOrder("drop table if exists `{0}`", tableName);
    }

    public String cleanQuery() {
        return Strings.replaceWithOrder("delete from `{0}`", tableName);
    }

    public String truncateQuery() {
        return Strings.replaceWithOrder("truncate table `{0}`", tableName);
    }

    public RunnableQuery executeQuery(String query) {
        return new RunnableQuery(query);
    }

    public RunnableQuery executeSelect() {
        return executeQuery("select * from " + tableName);
    }

    public RunnableQuery executeSelect(String queryWhere) {
        return executeQuery("select * from " + tableName + " where " + queryWhere);
    }

    public RunnableUpdate executeInsert(String queryValues) {
        return executeUpdate("insert into " + tableName + " values(" + queryValues + ")");
    }

    public RunnableUpdate executeUpdate(String query) {
        return new RunnableUpdate(query);
    }

    public RunnableUpdate executeUpdate(String update, String where) {
        return executeUpdate("update " + tableName + " set " + update + " where " + where);
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
