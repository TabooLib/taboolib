package io.izzel.taboolib.module.nms.nbt;

import com.google.common.collect.Maps;
import com.google.gson.*;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.util.Strings;
import org.bukkit.inventory.ItemStack;

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

    public void saveTo(ItemStack item) {
        item.setItemMeta(NMS.handle().saveNBT(item, this).getItemMeta());
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public String toJsonFormatted() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(new Gson().toJsonTree(this));
    }

    @Override
    public String toJsonSimplified() {
        return toJsonSimplified(0);
    }

    @Override
    public String toJsonSimplified(int index) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        value.forEach((k, v) -> {
            builder.append(Strings.copy("  ", index + 1))
                    .append("\"")
                    .append(k)
                    .append("\"")
                    .append(": ")
                    .append(v.toJsonSimplified(index + 1))
                    .append("\n");
        });
        builder.append(Strings.copy("  ", index)).append("}");
        return builder.toString();
    }

    public static NBTCompound fromJson(String json) {
        return (NBTCompound) fromJson(new JsonParser().parse(json));
    }

    static NBTBase fromJson(JsonElement element) {
        if (element instanceof JsonObject) {
            JsonObject json = (JsonObject) element;
            // base
            if (json.has("type") && json.has("data") && json.size() == 2) {
                switch (NBTType.parse(json.get("type").getAsString())) {
                    case BYTE:
                        return new NBTBase(json.get("data").getAsByte());
                    case SHORT:
                        return new NBTBase(json.get("data").getAsShort());
                    case INT:
                        return new NBTBase(json.get("data").getAsInt());
                    case LONG:
                        return new NBTBase(json.get("data").getAsLong());
                    case FLOAT:
                        return new NBTBase(json.get("data").getAsFloat());
                    case DOUBLE:
                        return new NBTBase(json.get("data").getAsDouble());
                    case STRING:
                        return new NBTBase(json.get("data").getAsString());
                    case BYTE_ARRAY: {
                        JsonArray array = json.get("data").getAsJsonArray();
                        byte[] bytes = new byte[array.size()];
                        for (int i = 0; i < array.size(); i++) {
                            bytes[i] = array.get(i).getAsByte();
                        }
                        return new NBTBase(bytes);
                    }
                    case INT_ARRAY: {
                        JsonArray array = json.get("data").getAsJsonArray();
                        int[] ints = new int[array.size()];
                        for (int i = 0; i < array.size(); i++) {
                            ints[i] = array.get(i).getAsInt();
                        }
                        return new NBTBase(ints);
                    }
                    default:
                        return new NBTBase("error: " + element);
                }
            }
            // compound
            else {
                NBTCompound compound = new NBTCompound();
                for (Entry<String, JsonElement> elementEntry : json.entrySet()) {
                    compound.put(elementEntry.getKey(), fromJson(elementEntry.getValue()));
                }
                return compound;
            }
        }
        // list
        else if (element instanceof JsonArray) {
            NBTList list = new NBTList();
            for (JsonElement jsonElement : (JsonArray) element) {
                list.add(fromJson(jsonElement));
            }
            return list;
        }
        return new NBTBase("error: " + element);
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

    public NBTBase getDeep(String key) {
        NBTBase value = this;
        for (String keyStr : key.split("\\.")) {
            if ((value = value.asCompound().get(keyStr)) == null) {
                return null;
            }
        }
        return value;
    }

    @Override
    public NBTBase put(String key, NBTBase value) {
        return this.value.put(key, value);
    }

    public NBTBase put(String key, Object value) {
        return this.value.put(key, NBTBase.toNBT(value));
    }

    public NBTBase putDeep(String key, Object value) {
        return putDeep(key, NBTBase.toNBT(value));
    }

    public NBTBase putDeep(String key, NBTBase value) {
        NBTBase compound = this, temp;
        String[] keySplit = key.split("\\.");
        for (String keyStr : keySplit) {
            if (keyStr.equalsIgnoreCase(keySplit[keySplit.length - 1])) {
                return ((NBTCompound) compound).put(keyStr, value);
            }
            if ((temp = compound.asCompound().get(keyStr)) == null) {
                temp = new NBTCompound();
                compound.asCompound().put(keyStr, temp);
            }
            compound = temp;
        }
        return null;
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
        return this.value.getOrDefault(String.valueOf(key), defaultValue);
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
