package io.izzel.taboolib.util.serialize;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    default Object readBase64(String value) {
        return TSerializer.read(this, new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8));
    }

    default String writeBase64() {
        return Base64.getEncoder().encodeToString(TSerializer.write(this).getBytes(StandardCharsets.UTF_8));
    }
}