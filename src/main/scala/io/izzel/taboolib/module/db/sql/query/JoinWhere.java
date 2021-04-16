package io.izzel.taboolib.module.db.sql.query;

import io.izzel.taboolib.module.db.sql.SQLTable;

/**
 * io.izzel.taboolib.module.db.sql.query.JoinField
 *
 * @author sky
 * @since 2021/4/16 9:10 上午
 */
public class JoinWhere {

    private final String leftTable;
    private final String leftField;
    private final String rightTable;
    private final String rightField;
    private final String symbol;

    JoinWhere(String leftTable, String leftField, String rightTable, String rightField, String symbol) {
        this.leftTable = leftTable;
        this.leftField = leftField;
        this.rightTable = rightTable;
        this.rightField = rightField;
        this.symbol = symbol;
    }

    public static JoinWhere of(String leftTable, String leftField, String symbol, String rightTable, String rightField) {
        return new JoinWhere(leftTable, leftField, rightTable, rightField, symbol);
    }

    public static JoinWhere equals(String leftTable, String leftField, String rightTable, String rightField) {
        return new JoinWhere(leftTable, leftField, rightTable, rightField, "=");
    }

    public static JoinWhere of(SQLTable leftTable, String leftField, String symbol, SQLTable rightTable, String rightField) {
        return new JoinWhere(leftTable.getTableName(), leftField, rightTable.getTableName(), rightField, symbol);
    }

    public static JoinWhere equals(SQLTable leftTable, String leftField, SQLTable rightTable, String rightField) {
        return new JoinWhere(leftTable.getTableName(), leftField, rightTable.getTableName(), rightField, "=");
    }

    public String getLeftTable() {
        return leftTable;
    }

    public String getLeftField() {
        return leftField;
    }

    public String getRightTable() {
        return rightTable;
    }

    public String getRightField() {
        return rightField;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "JoinWhere{" +
                "leftTable='" + leftTable + '\'' +
                ", leftField='" + leftField + '\'' +
                ", rightTable='" + rightTable + '\'' +
                ", rightField='" + rightField + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
