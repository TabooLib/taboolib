package me.skymc.taboolib.common.serialize;

/**
 * @Author 坏黑
 * @Since 2019-03-08 17:28
 */
public interface TSerializable {

    void read(String fieldName, String value);

    String write(String fieldName, Object value);

    default void read(String value) {
        TSerializer.read(this, value);
    }

    default String write() {
        return TSerializer.write(this);
    }
}