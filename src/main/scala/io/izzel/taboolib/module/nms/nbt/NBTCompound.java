package io.izzel.taboolib.module.nms.nbt;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @Author 坏黑
 * @Since 2019-05-24 17:44
 */
public class NBTCompound extends NBTBase implements Map<String, NBTBase> {

    private Map<String, NBTBase> value = Maps.newConcurrentMap();

    public NBTCompound() {
        super(0);
        this.type = NBTType.COMPOUND;
        this.data = this;
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return value.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.value.containsValue(value);
    }

    @Override
    public NBTBase get(Object key) {
        return value.get(key);
    }

    @Override
    public NBTBase put(String key, NBTBase value) {
        return this.value.put(key, value);
    }

    @Override
    public NBTBase remove(Object key) {
        return value.remove(key);
    }

    @Override
    public void putAll(Map m) {
        this.value.putAll(m);
    }

    @Override
    public void clear() {
        this.value.clear();
    }

    @Override
    public Set keySet() {
        return this.value.keySet();
    }

    @Override
    public Collection values() {
        return this.value.values();
    }

    @Override
    public Set<Entry<String, NBTBase>> entrySet() {
        return this.value.entrySet();
    }

    @Override
    public NBTBase getOrDefault(Object key, NBTBase defaultValue) {
        return this.value.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super NBTBase> action) {
        this.value.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super NBTBase, ? extends NBTBase> function) {
        this.value.replaceAll(function);
    }

    @Override
    public NBTBase putIfAbsent(String key, NBTBase value) {
        return this.value.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return this.value.remove(key, value);
    }

    @Override
    public boolean replace(String key, NBTBase oldValue, NBTBase newValue) {
        return this.value.replace(key, oldValue, newValue);
    }

    @Override
    public NBTBase replace(String key, NBTBase value) {
        return this.value.replace(key, value);
    }

    @Override
    public NBTBase computeIfAbsent(String key, Function<? super String, ? extends NBTBase> mappingFunction) {
        return this.value.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public NBTBase computeIfPresent(String key, BiFunction<? super String, ? super NBTBase, ? extends NBTBase> remappingFunction) {
        return this.value.computeIfPresent(key, remappingFunction);
    }

    @Override
    public NBTBase compute(String key, BiFunction<? super String, ? super NBTBase, ? extends NBTBase> remappingFunction) {
        return this.value.compute(key, remappingFunction);
    }

    @Override
    public NBTBase merge(String key, NBTBase value, BiFunction<? super NBTBase, ? super NBTBase, ? extends NBTBase> remappingFunction) {
        return this.value.merge(key, value, remappingFunction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NBTCompound)) {
            return false;
        }
        NBTCompound that = (NBTCompound) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
