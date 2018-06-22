package me.skymc.taboolib.mysql.builder;

import com.google.common.base.Preconditions;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.string.ArrayUtils;

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
            columns = new SQLColumn[]{sqlColumn};
        } else {
            ArrayUtils.arrayAppend(columns, sqlColumn);
        }
        return this;
    }

    public String createQuery() {
        Preconditions.checkNotNull(columns);
        StringBuilder builder = new StringBuilder();
        for (SQLColumn sqlColumn : columns) {
            builder.append(sqlColumn.convertToCommand()).append(", ");
        }
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
