package io.izzel.taboolib.module.packet;

import io.izzel.taboolib.module.lite.SimpleReflection;

import java.util.Arrays;

/**
 * @Author sky
 * @Since 2019-10-25 22:52
 */
public class Packet {

    private Object origin;
    private Class<?> packetClass;

    public Packet(Object origin) {
        this.origin = origin;
        this.packetClass = origin.getClass();
        SimpleReflection.checkAndSave(this.packetClass);
    }

    public boolean is(Class<?> packetClass) {
        return this.packetClass.equals(packetClass);
    }

    public boolean is(String packetName) {
        return this.packetClass.getSimpleName().equalsIgnoreCase(packetName);
    }

    public boolean any(Class<?>... packetClass) {
        return Arrays.stream(packetClass).anyMatch(this::is);
    }

    public boolean any(String... packetClass) {
        return Arrays.stream(packetClass).anyMatch(this::is);
    }

    public Object read(String key) {
        return SimpleReflection.getFieldValue(this.packetClass, origin, key);
    }

    public <T> T read(String key, T def) {
        return SimpleReflection.getFieldValue(this.packetClass, origin, key, def);
    }

    public <T> T read(String key, Class<? extends T> type) {
        Object value = SimpleReflection.getFieldValue(this.packetClass, origin, key);
        return value == null ? null : (T) value;
    }

    public void write(String key, Object value) {
        SimpleReflection.setFieldValue(this.packetClass, origin, key, value);
    }

    public Packet copy(String... copyField) {
        return copy(packetClass, copyField);
    }

    public Packet copy(Class clazz, String... copyField) {
        if (clazz == null) {
            clazz = this.packetClass;
        }
        Object packet;
        try {
            packet = clazz.newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
        for (String field : copyField) {
            SimpleReflection.setFieldValue(clazz, packet, field, SimpleReflection.getFieldValue(clazz, origin, field));
        }
        return new Packet(packet);
    }

    public Object get() {
        return origin;
    }
}
