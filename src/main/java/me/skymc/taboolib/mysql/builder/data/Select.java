package me.skymc.taboolib.mysql.builder.data;

/**
 * @Author 坏黑
 * @Since 2018-12-02 11:12
 */
public class Select {

    private String[] column;

    public Select(String... column) {
        this.column = column;
    }

    public String[] getColumn() {
        return column;
    }

    public static Select of(String... column) {
        return new Select(column);
    }
}
