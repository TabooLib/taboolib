package me.skymc.taboolib.mysql.builder.data;

/**
 * @Author 坏黑
 * @Since 2018-12-01 23:32
 */
public enum Insert {

    NULL("null"),

    VARIABLE("?");

    private String text;

    Insert(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
