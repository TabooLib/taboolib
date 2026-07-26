package taboolib.module.lang;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 保持 HashMap API 的无锁快照映射，读取固定快照，写入基于快照复制后整体替换引用。
 *
 * <p>之所以继承 {@link HashMap} 而非实现 {@link Map}，是因为 {@code Language.languageFile} 与
 * {@code LanguageFile.nodes} 的公开声明类型就是 {@code HashMap}，改成接口会破坏 ABI 兼容性。
 * 代价是本类的所有状态都存放在 {@link #snapshot} 中，<b>父类 HashMap 自身的桶数组永远是空的</b>，
 * 由此带来两点必须注意的限制：
 *
 * <ol>
 *   <li><b>不支持序列化还原。</b>{@code HashMap.writeObject} 是 {@code private} 的，无法覆盖，
 *       它只会遍历父类自己（永远为空）的桶数组。为避免静默写出空 map，本类通过
 *       {@code writeReplace()} 改为写出快照副本；但反序列化得到的是普通 {@code HashMap}，
 *       快照语义不会被还原。请勿依赖本类的序列化往返。</li>
 *   <li><b>JDK 升级时需复查。</b>本类采用"逐方法代理"的方式覆盖了 {@code Map} / {@code HashMap}
 *       的全部读写入口。若后续 JDK 为 {@code Map} 或 {@code HashMap} 新增了默认方法或实例方法
 *       而未在此同步覆盖，该方法会读到空的父类状态并返回错误结果。
 *       升级 JDK 基线时请对照新版 API 列表复查一遍覆盖完整性。</li>
 * </ol>
 */
final class SnapshotHashMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private final AtomicReference<HashMap<K, V>> snapshot;

    SnapshotHashMap() {
        this(new HashMap<>());
    }

    SnapshotHashMap(Map<? extends K, ? extends V> source) {
        snapshot = new AtomicReference<>(new HashMap<>(source));
    }

    /**
     * 用 {@code source} 的副本整体替换当前快照。
     *
     * <p>这里用的是原子引用替换（{@code set}）而非 CAS：整体替换不依赖旧值，
     * 无需比较，因此不存在需要重试的写冲突。并发读取要么看到完整的旧快照，
     * 要么看到完整的新快照，不会读到 {@code clear() + putAll()} 那样的中间态。
     */
    void replaceWith(Map<? extends K, ? extends V> source) {
        snapshot.set(new HashMap<>(source));
    }

    @Override
    public int size() {
        return snapshot.get().size();
    }

    @Override
    public boolean isEmpty() {
        return snapshot.get().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return snapshot.get().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return snapshot.get().containsValue(value);
    }

    @Override
    public V get(Object key) {
        return snapshot.get().get(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return snapshot.get().getOrDefault(key, defaultValue);
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                Iterator<K> iterator = new HashMap<>(snapshot.get()).keySet().iterator();
                return new Iterator<K>() {
                    private K current;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public K next() {
                        current = iterator.next();
                        return current;
                    }

                    @Override
                    public void remove() {
                        SnapshotHashMap.this.remove(current);
                    }
                };
            }

            @Override
            public int size() {
                return SnapshotHashMap.this.size();
            }

            @Override
            public boolean contains(Object value) {
                return SnapshotHashMap.this.containsKey(value);
            }

            @Override
            public boolean remove(Object value) {
                boolean present = SnapshotHashMap.this.containsKey(value);
                SnapshotHashMap.this.remove(value);
                return present;
            }

            @Override
            public void clear() {
                SnapshotHashMap.this.clear();
            }
        };
    }

    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                Iterator<Entry<K, V>> iterator = new HashMap<>(snapshot.get()).entrySet().iterator();
                return new Iterator<V>() {
                    private Entry<K, V> current;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public V next() {
                        current = iterator.next();
                        return current.getValue();
                    }

                    @Override
                    public void remove() {
                        SnapshotHashMap.this.remove(current.getKey(), current.getValue());
                    }
                };
            }

            @Override
            public int size() {
                return SnapshotHashMap.this.size();
            }

            @Override
            public boolean contains(Object value) {
                return SnapshotHashMap.this.containsValue(value);
            }

            @Override
            public boolean remove(Object value) {
                for (Entry<K, V> entry : snapshot.get().entrySet()) {
                    if (Objects.equals(entry.getValue(), value)) {
                        return SnapshotHashMap.this.remove(entry.getKey(), entry.getValue());
                    }
                }
                return false;
            }

            @Override
            public void clear() {
                SnapshotHashMap.this.clear();
            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                Iterator<Entry<K, V>> iterator = new HashMap<>(snapshot.get()).entrySet().iterator();
                return new Iterator<Entry<K, V>>() {
                    private Entry<K, V> current;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        current = iterator.next();
                        K key = current.getKey();
                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return key;
                            }

                            @Override
                            public V getValue() {
                                return SnapshotHashMap.this.get(key);
                            }

                            @Override
                            public V setValue(V value) {
                                return SnapshotHashMap.this.put(key, value);
                            }

                            @Override
                            public boolean equals(Object other) {
                                if (!(other instanceof Entry)) {
                                    return false;
                                }
                                Entry<?, ?> entry = (Entry<?, ?>) other;
                                return Objects.equals(key, entry.getKey()) && Objects.equals(getValue(), entry.getValue());
                            }

                            @Override
                            public int hashCode() {
                                return Objects.hashCode(key) ^ Objects.hashCode(getValue());
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        SnapshotHashMap.this.remove(current.getKey(), current.getValue());
                    }
                };
            }

            @Override
            public int size() {
                return SnapshotHashMap.this.size();
            }

            @Override
            public boolean contains(Object value) {
                if (!(value instanceof Entry)) {
                    return false;
                }
                Entry<?, ?> entry = (Entry<?, ?>) value;
                return SnapshotHashMap.this.containsKey(entry.getKey())
                        && Objects.equals(SnapshotHashMap.this.get(entry.getKey()), entry.getValue());
            }

            @Override
            public boolean remove(Object value) {
                if (!(value instanceof Entry)) {
                    return false;
                }
                Entry<?, ?> entry = (Entry<?, ?>) value;
                return SnapshotHashMap.this.remove(entry.getKey(), entry.getValue());
            }

            @Override
            public void clear() {
                SnapshotHashMap.this.clear();
            }
        };
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        snapshot.get().forEach(action);
    }

    @Override
    public V put(K key, V value) {
        return mutate(copy -> copy.put(key, value));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        mutate(copy -> {
            copy.putAll(map);
            return null;
        });
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return mutate(copy -> copy.putIfAbsent(key, value));
    }

    @Override
    public V remove(Object key) {
        return mutate(copy -> copy.remove(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return mutate(copy -> copy.remove(key, value));
    }

    @Override
    public V replace(K key, V value) {
        return mutate(copy -> copy.replace(key, value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return mutate(copy -> copy.replace(key, oldValue, newValue));
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        mutate(copy -> {
            copy.replaceAll(function);
            return null;
        });
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return mutate(copy -> copy.computeIfAbsent(key, mappingFunction));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return mutate(copy -> copy.computeIfPresent(key, remappingFunction));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return mutate(copy -> copy.compute(key, remappingFunction));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return mutate(copy -> copy.merge(key, value, remappingFunction));
    }

    @Override
    public void clear() {
        snapshot.set(new HashMap<>());
    }

    /**
     * 返回当前快照的普通 {@code HashMap} 副本。
     *
     * <p>返回值不再具备快照语义，与本实例互相独立。
     */
    @Override
    public Object clone() {
        return new HashMap<>(snapshot.get());
    }

    /**
     * 序列化替身：写出普通 {@code HashMap} 副本。
     *
     * <p>父类的 {@code writeObject} 是 {@code private} 的，无法覆盖，
     * 直接序列化本类只会写出永远为空的父类桶数组。此处改写为快照副本，
     * 使序列化结果至少携带真实数据；但反序列化得到的是 {@code HashMap}
     * 而非 {@code SnapshotHashMap}，快照语义不会被还原。
     */
    private Object writeReplace() {
        return new HashMap<>(snapshot.get());
    }

    @Override
    public boolean equals(Object other) {
        return snapshot.get().equals(other);
    }

    @Override
    public int hashCode() {
        return snapshot.get().hashCode();
    }

    @Override
    public String toString() {
        return snapshot.get().toString();
    }

    private <R> R mutate(Function<HashMap<K, V>, R> operation) {
        while (true) {
            HashMap<K, V> current = snapshot.get();
            HashMap<K, V> updated = new HashMap<>(current);
            R result = operation.apply(updated);
            if (snapshot.compareAndSet(current, updated)) {
                return result;
            }
        }
    }
}
