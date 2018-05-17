package me.skymc.taboolib.mysql.builder;

/**
 * @Author sky
 * @Since 2018-05-14 19:07
 */
public class SQLTable {

    private final String tableName;
    private final SQLColumn[] columns;

    public SQLTable(String tableName, SQLColumn... columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public SQLColumn[] getColumns() {
        return columns;
    }
}
