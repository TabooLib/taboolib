package taboolib.module.nms;

import org.jetbrains.annotations.NotNull;

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
public class ItemTagList extends ItemTagData implements List<ItemTagData> {

    private final List<ItemTagData> value = new CopyOnWriteArrayList<>();

    public ItemTagList() {
        super(0);
        this.type = ItemTagType.LIST;
        this.data = this;
    }

    public static ItemTagList of(ItemTagData... base) {
        ItemTagList list = new ItemTagList();
        list.addAll(Arrays.asList(base));
        return list;
    }

    public static ItemTagList of(Object... base) {
        ItemTagList list = new ItemTagList();
        for (Object obj : base) {
            list.add(ItemTagData.toNBT(obj));
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
            builder.append(copy("  ", index + 1))
                    .append(v.toJsonSimplified(index + 1))
                    .append("\n");
        });
        builder.append(copy("  ", index)).append("]");
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

    @NotNull
    @Override
    public Iterator<ItemTagData> iterator() {
        return value.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return value.toArray();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return (T[]) value.toArray();
    }

    @Override
    public boolean add(ItemTagData base) {
        return value.add(base);
    }

    @Override
    public boolean remove(Object o) {
        return value.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return value.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ItemTagData> c) {
        return value.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends ItemTagData> c) {
        return value.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return value.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super ItemTagData> filter) {
        return value.removeIf(filter);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return value.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<ItemTagData> operator) {
        value.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super ItemTagData> c) {
        value.sort(c);
    }

    @Override
    public void clear() {
        value.clear();
    }

    @Override
    public ItemTagData get(int index) {
        return value.get(index);
    }

    @Override
    public ItemTagData set(int index, ItemTagData element) {
        return value.set(index, element);
    }

    @Override
    public void add(int index, ItemTagData element) {
        value.add(index, element);
    }

    @Override
    public ItemTagData remove(int index) {
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

    @NotNull
    @Override
    public ListIterator<ItemTagData> listIterator() {
        return value.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<ItemTagData> listIterator(int index) {
        return value.listIterator(index);
    }

    @NotNull
    @Override
    public List<ItemTagData> subList(int fromIndex, int toIndex) {
        return value.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<ItemTagData> spliterator() {
        return value.spliterator();
    }

    @Override
    public Stream<ItemTagData> stream() {
        return value.stream();
    }

    @Override
    public Stream<ItemTagData> parallelStream() {
        return value.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super ItemTagData> action) {
        value.forEach(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemTagList)) {
            return false;
        }
        ItemTagList nbtBases = (ItemTagList) o;
        return Objects.equals(value, nbtBases.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return NMS_UTILS.itemTagToString(this);
    }
}
