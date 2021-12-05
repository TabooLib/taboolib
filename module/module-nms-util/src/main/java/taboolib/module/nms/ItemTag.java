package taboolib.module.nms;

import com.google.common.collect.Maps;
import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 物品 NBT 结构映射类
 *
 * @author 坏黑
 * @since 2019-05-24 17:44
 */
public class ItemTag extends ItemTagData implements Map<String, ItemTagData> {

    private final Map<String, ItemTagData> value = Maps.newConcurrentMap();

    public ItemTag() {
        super(0);
        this.type = ItemTagType.COMPOUND;
        this.data = this;
    }

    public void saveTo(ItemStack item) {
        item.setItemMeta(NMSKt.setItemTag(item, this).getItemMeta());
    }

    public String toJson() {
        return ItemTagSerializer.INSTANCE.serializeData(this).toString();
    }

    public String toJsonFormatted() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(ItemTagSerializer.INSTANCE.serializeTag(this));
    }

    @Override
    public String toJsonSimplified() {
        return toJsonSimplified(0);
    }

    @Override
    public String toJsonSimplified(int index) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        value.forEach((k, v) -> builder.append(copy("  ", index + 1))
                .append("\"")
                .append(k)
                .append("\"")
                .append(": ")
                .append(v.toJsonSimplified(index + 1))
                .append("\n"));
        builder.append(copy("  ", index)).append("}");
        return builder.toString();
    }

    public static ItemTag fromJson(String json) {
        return fromJson(new JsonParser().parse(json)).asCompound();
    }

    public static ItemTagData fromJson(JsonElement element) {
        return ItemTagSerializer.INSTANCE.deserializeData(element);
    }

    @Deprecated
    public String toLegacyJson() {
        return new Gson().toJson(this);
    }

    @Deprecated
    public static ItemTag fromLegacyJson(String json) {
        return fromLegacyJson(new JsonParser().parse(json)).asCompound();
    }

    @Deprecated
    public static ItemTagData fromLegacyJson(JsonElement element) {
        if (element instanceof JsonObject) {
            JsonObject json = (JsonObject) element;
            // base
            if (json.has("type") && json.has("data") && json.entrySet().size() == 2) {
                switch (ItemTagType.parse(json.get("type").getAsString())) {
                    case BYTE:
                        return new ItemTagData(json.get("data").getAsByte());
                    case SHORT:
                        return new ItemTagData(json.get("data").getAsShort());
                    case INT:
                        return new ItemTagData(json.get("data").getAsInt());
                    case LONG:
                        return new ItemTagData(json.get("data").getAsLong());
                    case FLOAT:
                        return new ItemTagData(json.get("data").getAsFloat());
                    case DOUBLE:
                        return new ItemTagData(json.get("data").getAsDouble());
                    case STRING:
                        return new ItemTagData(json.get("data").getAsString());
                    case BYTE_ARRAY: {
                        JsonArray array = json.get("data").getAsJsonArray();
                        byte[] bytes = new byte[array.size()];
                        for (int i = 0; i < array.size(); i++) {
                            bytes[i] = array.get(i).getAsByte();
                        }
                        return new ItemTagData(bytes);
                    }
                    case INT_ARRAY: {
                        JsonArray array = json.get("data").getAsJsonArray();
                        int[] ints = new int[array.size()];
                        for (int i = 0; i < array.size(); i++) {
                            ints[i] = array.get(i).getAsInt();
                        }
                        return new ItemTagData(ints);
                    }
                    default:
                        return new ItemTagData("error: " + element);
                }
            }
            // compound
            else {
                ItemTag compound = new ItemTag();
                for (Entry<String, JsonElement> elementEntry : json.entrySet()) {
                    compound.put(elementEntry.getKey(), fromLegacyJson(elementEntry.getValue()));
                }
                return compound;
            }
        }
        // list
        else if (element instanceof JsonArray) {
            ItemTagList list = new ItemTagList();
            for (JsonElement jsonElement : (JsonArray) element) {
                list.add(fromLegacyJson(jsonElement));
            }
            return list;
        }
        return new ItemTagData("error: " + element);
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
    public ItemTagData get(Object key) {
        return value.get(key);
    }

    public ItemTagData getOrElse(String key, ItemTagData base) {
        return value.getOrDefault(key, base);
    }

    public ItemTagData getDeep(String key) {
        ItemTagData value = this;
        for (String keyStr : key.split("\\.")) {
            if ((value = value.asCompound().get(keyStr)) == null) {
                return null;
            }
        }
        return value;
    }

    public ItemTagData getDeepOrElse(String key, ItemTagData base) {
        return Optional.ofNullable(getDeep(key)).orElse(base);
    }

    @Override
    public ItemTagData put(String key, ItemTagData value) {
        return this.value.put(key, value);
    }

    public ItemTagData put(String key, Object value) {
        return this.value.put(key, ItemTagData.toNBT(value));
    }

    public ItemTagData putDeep(String key, Object value) {
        return putDeep(key, ItemTagData.toNBT(value));
    }

    public ItemTagData putDeep(String key, ItemTagData value) {
        ItemTagData compound = this, temp;
        String[] split = key.split("\\.");
        for (String node : split) {
            if (node.equalsIgnoreCase(split[split.length - 1])) {
                return ((ItemTag) compound).put(node, value);
            }
            if ((temp = compound.asCompound().get(node)) == null) {
                temp = new ItemTag();
                compound.asCompound().put(node, temp);
            }
            compound = temp;
        }
        return null;
    }

    public ItemTagData removeDeep(String key) {
        ItemTagData compound = this, temp;
        String[] split = key.split("\\.");
        for (String node : split) {
            if (node.equalsIgnoreCase(split[split.length - 1])) {
                return ((ItemTag) compound).remove(node);
            }
            if ((temp = compound.asCompound().get(node)) == null) {
                return null;
            }
            compound = temp;
        }
        return null;
    }

    @Override
    public ItemTagData remove(Object key) {
        return value.remove(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(@NotNull Map m) {
        this.value.putAll(m);
    }

    @Override
    public void clear() {
        this.value.clear();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @NotNull
    @Override
    public Set keySet() {
        return this.value.keySet();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @NotNull
    @Override
    public Collection values() {
        return this.value.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, ItemTagData>> entrySet() {
        return this.value.entrySet();
    }

    @Override
    public ItemTagData getOrDefault(Object key, ItemTagData defaultValue) {
        return this.value.getOrDefault(String.valueOf(key), defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super ItemTagData> action) {
        this.value.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super ItemTagData, ? extends ItemTagData> function) {
        this.value.replaceAll(function);
    }

    @Override
    public ItemTagData putIfAbsent(String key, ItemTagData value) {
        return this.value.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return this.value.remove(key, value);
    }

    @Override
    public boolean replace(String key, ItemTagData oldValue, ItemTagData newValue) {
        return this.value.replace(key, oldValue, newValue);
    }

    @Override
    public ItemTagData replace(String key, ItemTagData value) {
        return this.value.replace(key, value);
    }

    @Override
    public ItemTagData computeIfAbsent(String key, @NotNull Function<? super String, ? extends ItemTagData> mappingFunction) {
        return this.value.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public ItemTagData computeIfPresent(String key, @NotNull BiFunction<? super String, ? super ItemTagData, ? extends ItemTagData> remappingFunction) {
        return this.value.computeIfPresent(key, remappingFunction);
    }

    @Override
    public ItemTagData compute(String key, @NotNull BiFunction<? super String, ? super ItemTagData, ? extends ItemTagData> remappingFunction) {
        return this.value.compute(key, remappingFunction);
    }

    @Override
    public ItemTagData merge(String key, @NotNull ItemTagData value, @NotNull BiFunction<? super ItemTagData, ? super ItemTagData, ? extends ItemTagData> remappingFunction) {
        return this.value.merge(key, value, remappingFunction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemTag)) {
            return false;
        }
        ItemTag that = (ItemTag) o;
        return Objects.equals(value, that.value);
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
