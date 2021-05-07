package io.izzel.taboolib.module.db.sql.query;

import io.izzel.taboolib.util.Strings;

/**
 * @author sky
 * @since 2019-10-26 14:02
 */
public class Order {

    private final String row;
    private final boolean desc;

    public Order(String row) {
        this.row = row;
        this.desc = false;
    }

    public Order(String row, boolean desc) {
        this.row = row;
        this.desc = desc;
    }

    public String toQuery(String tableName) {
        return Strings.replaceWithOrder("`{0}.{1}` {2}", tableName, row, (desc ? "desc" : "asc"));
    }
}
