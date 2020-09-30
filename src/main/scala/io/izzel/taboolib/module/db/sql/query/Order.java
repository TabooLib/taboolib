package io.izzel.taboolib.module.db.sql.query;

/**
 * @Author sky
 * @Since 2019-10-26 14:02
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

    public String toQuery() {
        return row + " " + (desc ? "desc" : "asc");
    }
}
