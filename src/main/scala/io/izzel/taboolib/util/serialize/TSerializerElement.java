package io.izzel.taboolib.util.serialize;

/**
 * @Author 坏黑
 * @Since 2019-03-08 17:28
 */
public interface TSerializerElement<T> {

    T read(String value);

    String write(T value);

    boolean matches(Class objectClass);
}