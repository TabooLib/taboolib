package io.izzel.taboolib.module.nms.nbt;

import io.izzel.taboolib.util.Strings;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * 物品 NBT 结构映射类
 *
 * @author 坏黑
 * @since 2019-05-24 18:37
 */
public class NBTList extends NBTBase implements List<NBTBase> {

    private final List<NBTBase> value = new CopyOnWriteArrayList<>();

    public NBTList() {
        super(0);
        this.type = NBTType.LIST;
        this.data = this;
    }

    public static NBTList of(NBTBase... base) {
        NBTList list = new NBTList();
        list.addAll(Arrays.asList(base));
        return list;
    }

    public static NBTList of(Object... base) {
        NBTList list = new NBTList();
        for (Object obj : base) {
            list.add(NBTBase.toNBT(obj));
        }
        return list;
    }

    @Override
    public String toJsonSimplified() {
        return toJsonSimplified(0);
    }

    @Override
    public String toJsonSimplified(int index) {
        StringBuilder builder = new StringBuilder();
        builder.append("[\n");
        value.forEach(v -> {
            builder.append(Strings.copy("  ", index + 1))
                    .append(v.toJsonSimplified(index + 1))
                    .append("\n");
        });
        builder.append(Strings.copy("  ", index)).append("]");
        return builder.toString();
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
    public boolean contains(Object o) {
        return value.contains(0);
    }

    @Override
    public Iterator<NBTBase> iterator() {
        return value.iterator();
    }

    @Override
    public Object[] toArray() {
        return value.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return value.toArray(a);
    }

    @Override
    public boolean add(NBTBase base) {
        return value.add(base);
    }

    @Override
    public boolean remove(Object o) {
        return value.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return value.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends NBTBase> c) {
        return value.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends NBTBase> c) {
        return value.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return value.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super NBTBase> filter) {
        return value.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return value.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<NBTBase> operator) {
        value.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super NBTBase> c) {
        value.sort(c);
    }

    @Override
    public void clear() {
        value.clear();
    }

    @Override
    public NBTBase get(int index) {
        return value.get(index);
    }

    @Override
    public NBTBase set(int index, NBTBase element) {
        return value.set(index, element);
    }

    @Override
    public void add(int index, NBTBase element) {
        value.add(index, element);
    }

    @Override
    public NBTBase remove(int index) {
        return value.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return value.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return value.lastIndexOf(o);
    }

    @Override
    public ListIterator<NBTBase> listIterator() {
        return value.listIterator();
    }

    @Override
    public ListIterator<NBTBase> listIterator(int index) {
        return value.listIterator(index);
    }

    @Override
    public List<NBTBase> subList(int fromIndex, int toIndex) {
        return value.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<NBTBase> spliterator() {
        return value.spliterator();
    }

    @Override
    public Stream<NBTBase> stream() {
        return value.stream();
    }

    @Override
    public Stream<NBTBase> parallelStream() {
        return value.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super NBTBase> action) {
        value.forEach(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NBTList)) {
            return false;
        }
        NBTList nbtBases = (NBTList) o;
        return Objects.equals(value, nbtBases.value);
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
