package io.izzel.taboolib.util;

import java.util.Objects;

/**
 * @Author sky
 * @Since 2019-09-18 21:23
 */
public class KV<K, V> {

    private K key;
    private V value;

    public KV() {
    }

    public KV(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KV)) {
            return false;
        }
        KV<?, ?> kv = (KV<?, ?>) o;
        return Objects.equals(key, kv.key) &&
                Objects.equals(value, kv.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "KV{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
