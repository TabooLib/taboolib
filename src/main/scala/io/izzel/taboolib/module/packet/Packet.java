package io.izzel.taboolib.module.packet;

import com.google.common.base.Preconditions;
import io.izzel.taboolib.kotlin.Reflex;
import io.izzel.taboolib.module.lite.SimpleReflection;
import pw.yumc.Yum.reflect.Reflect;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author sky
 * @Since 2019-10-25 22:52
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
     * 获取 {@link Reflex} 实例
     */
    public Reflex reflex() {
        return reflex;
    }

    /**
     * 获取数据包中某个数据的 {@link Reflex} 实例
     */
    public Reflex reflex(String name) {
        Object obj = reflex.get(name);
        return Reflex.Companion.from(Objects.requireNonNull(obj).getClass(), obj);
    }

    /**
     * 检查数据包是否匹配
     *
     * @param packetClass 数据包类
     */
    public boolean is(Class<?> packetClass) {
        return this.packetClass.equals(packetClass);
    }

    /**
     * 检查数据包是否匹配
     *
     * @param packetName 数据包名称（忽略大小写）
     */
    public boolean is(String packetName) {
        return this.packetClass.getSimpleName().equalsIgnoreCase(packetName);
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
     */
    public Object read(String key) {
        return reflex.get(key);
    }

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
            reflex.set(field, this.reflex.get(field));
        }
        return new Packet(packet);
    }

    /**
     * 获取 nms 原始数据包实例
     */
    public Object get() {
        return origin;
    }
}
