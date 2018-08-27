package me.skymc.taboolib.mysql.builder;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.mysql.builder.query.RunnableQuery;
import me.skymc.taboolib.mysql.builder.query.RunnableUpdate;
import me.skymc.taboolib.string.ArrayUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;

/**
 * @Author sky
 * @Since 2018-05-14 19:07
 */
public class SQLTable {

    private String tableName;
    private SQLColumn[] columns;

    public SQLTable(String tableName) {
        this.tableName = tableName;
    }

    public SQLTable(String tableName, SQLColumn... column) {
        this.tableName = tableName;
        this.columns = column;
    }

    public SQLTable addColumn(SQLColumn sqlColumn) {
        if (columns == null) {
            columns = new SQLColumn[] {sqlColumn};
        } else {
            columns = ArrayUtils.arrayAppend(columns, sqlColumn);
        }
        return this;
    }

    public String createQuery() {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(columns).forEach(sqlColumn -> builder.append(sqlColumn.convertToCommand()).append(", "));
        return Strings.replaceWithOrder("create table if not exists `{0}` ({1})", tableName, builder.substring(0, builder.length() - 2));
    }

    public String deleteQuery() {
        return Strings.replaceWithOrder("drop table if exists `{0}`" + tableName);
    }

    public String cleanQuery() {
        return Strings.replaceWithOrder("delete from `{0}`" + tableName);
    }

    public String truncateQuery() {
        return Strings.replaceWithOrder("truncate table `{0}`", tableName);
    }

    public RunnableUpdate executeInsert(String values) {
        return executeUpdate("insert into " + tableName + " values(" + values + ")");
    }

    public RunnableQuery executeSelect(String where) {
        return executeQuery("select * from " + tableName + " where " + where);
    }

    public RunnableUpdate executeUpdate(String where, String update) {
        return executeUpdate("update " + tableName + " set " + update + " where " + where);
    }

    public RunnableUpdate executeUpdate(String query) {
        return new RunnableUpdate(query);
    }

    public RunnableQuery executeQuery(String query) {
        return new RunnableQuery(query);
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public String getTableName() {
        return tableName;
    }

    public SQLColumn[] getColumns() {
        return columns;
    }
}
