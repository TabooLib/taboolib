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
 * 保持 HashMap API 的无锁快照映射，读取固定快照，写入通过 CAS 一次替换。
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

    @Override
    public Object clone() {
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
