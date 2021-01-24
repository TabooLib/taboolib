package io.izzel.taboolib.module.packet;

import io.izzel.taboolib.kotlin.Reflex;

import java.util.Arrays;
import java.util.Objects;

/**
 * 数据包实例
 *
 * @author sky
 * @since 2019-10-25 22:52
 */
public class Packet {

    private final Object origin;
    private final Class<?> packetClass;
    private final Reflex reflex;

    /**
     * @param origin nms 数据包
     */
    public Packet(Object origin) {
        this.origin = origin;
        this.packetClass = origin.getClass();
        this.reflex = Reflex.Companion.from(packetClass, origin);
    }

    /**
     * @return {@link Reflex} 实例
     */
    public Reflex reflex() {
        return reflex;
    }

    /**
     * 获取数据包中某个数据的 {@link Reflex} 实例
     *
     * @param name 名称
     * @return {@link Reflex}
     */
    public Reflex reflex(String name) {
        Object obj = reflex.get(name);
        return Reflex.Companion.from(Objects.requireNonNull(obj).getClass(), obj);
    }

    /**
     * 检查数据包是否匹配
     *
     * @param packetClass 数据包类
     * @return boolean
     */
    public boolean is(Class<?> packetClass) {
        return this.packetClass.equals(packetClass);
    }

    /**
     * 检查数据包是否匹配
     *
     * @param packetName 数据包名称（忽略大小写）
     * @return boolean
     */
    public boolean is(String packetName) {
        return this.packetClass.getSimpleName().equalsIgnoreCase(packetName);
    }

    /**
     * 同 is() 方法
     *
     * @param packetName 数据包名称
     * @return boolean
     */
    public boolean equals(String packetName) {
        return this.is(packetName);
    }

    public boolean any(Class<?>... packetClass) {
        return Arrays.stream(packetClass).anyMatch(this::is);
    }

    public boolean any(String... packetClass) {
        return Arrays.stream(packetClass).anyMatch(this::is);
    }

    /**
     * 读取数据包中的某个内容
     *
     * @param key 名称
     * @return NMS Object
     */
    public Object read(String key) {
        return reflex.get(key);
    }

    /**
     * 读取数据包中的某个内容
     *
     * @param key 名称
     * @param def 默认值
     * @param <T> ?
     * @return NMS Object
     */
    public <T> T read(String key, T def) {
        T obj = reflex.get(key);
        return obj == null ? def : obj;
    }

    @Deprecated
    public <T> T read(String key, Class<? extends T> type) {
        return reflex.get(key);
    }

    /**
     * 向该数据包写入数据
     *
     * @param key   名称
     * @param value 数据
     */
    public void write(String key, Object value) {
        reflex.set(key, value);
    }

    /**
     * 克隆一份相同的数据包
     *
     * @param fields 克隆内容（指 field 名称）
     * @return 数据包实例
     */
    public Packet copy(String... fields) {
        return copy(packetClass, fields);
    }

    /**
     * 克隆一份相同的数据包
     *
     * @param clazz  类型
     * @param fields 克隆内容（指 field 名称）
     * @return 数据包实例
     */
    public Packet copy(Class<?> clazz, String... fields) {
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
        Reflex reflex = Reflex.Companion.from(clazz, packet);
        for (String field : fields) {
            try {
                reflex.set(field, this.reflex.get(field));
            } catch (Throwable t) {
                System.out.println("[TabooLib] Packet copy failed: " + field + " (" + clazz.getName() + ")");
                System.out.println("[TabooLib] Origin class: " + this.packetClass.getName());
                System.out.println("[TabooLib] Origin value: " + this.reflex.get(field) + " (" + this.reflex.get(field).getClass().getName() + ")");
                t.printStackTrace();
                throw t;
            }
        }
        return new Packet(packet);
    }

    /**
     * @return nms 原始数据包实例
     */
    public Object get() {
        return origin;
    }
}
