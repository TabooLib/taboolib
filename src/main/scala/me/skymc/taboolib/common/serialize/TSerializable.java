package me.skymc.taboolib.common.serialize;

/**
 * @Author 坏黑
 * @Since 2019-03-08 17:28
 */
public interface TSerializable {

    default void read(String fieldName, String value) {
    }

    default String write(String fieldName, Object value) {
        return null;
    }

    default Object read(String value) {
        return TSerializer.read(this, value);
    }

    default String write() {
        return TSerializer.write(this);
    }
}