package taboolib.module.nms;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;
import taboolib.common.platform.function.IOKt;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 物品 NBT 结构映射类
 *
 * @author 坏黑
 * @since 2019-05-24 17:45
 */
@SuppressWarnings("ALL")
public class ItemTagData {

    protected static final NMSGeneric NMS_UTILS = MinecraftServerUtilKt.nmsProxy(NMSGeneric.class, "{name}Impl");
    protected static final Pattern SHORT_PATTERN = Pattern.compile("\\d+s");
    protected ItemTagType type;
    protected Object data;

    public ItemTagData(String data) {
        this.type = ItemTagType.STRING;
        this.data = data;
    }

    public ItemTagData(byte data) {
        this.type = ItemTagType.BYTE;
        this.data = data;
    }

    public ItemTagData(byte[] data) {
        this.type = ItemTagType.BYTE_ARRAY;
        this.data = data;
    }

    public ItemTagData(int data) {
        this.type = ItemTagType.INT;
        this.data = data;
    }

    public ItemTagData(int[] data) {
        this.type = ItemTagType.INT_ARRAY;
        this.data = data;
    }

    public ItemTagData(double data) {
        this.type = ItemTagType.DOUBLE;
        this.data = data;
    }

    public ItemTagData(float data) {
        this.type = ItemTagType.FLOAT;
        this.data = data;
    }

    public ItemTagData(short data) {
        this.type = ItemTagType.SHORT;
        this.data = data;
    }

    public ItemTagData(long data) {
        this.type = ItemTagType.LONG;
        this.data = data;
    }

    public ItemTagData(ItemTag data) {
        this.type = ItemTagType.COMPOUND;
        this.data = data;
    }

    public ItemTagData(ItemTagList data) {
        this.type = ItemTagType.LIST;
        this.data = data;
    }

    public String toJsonSimplified() {
        return toJsonSimplified(0);
    }

    public String toJsonSimplified(int index) {
        return data instanceof String ? "\"" + data + "\"" : toString();
    }

    public Object unsafeData() {
        return data;
    }

    public String asString() {
        return String.valueOf(data);
    }

    public byte asByte() {
        return NumberConversions.toByte(data);
    }

    public byte[] asByteArray() {
        return (byte[]) data;
    }

    public int asInt() {
        return NumberConversions.toInt(data);
    }

    public int[] asIntArray() {
        return (int[]) data;
    }

    public double asDouble() {
        return NumberConversions.toDouble(data);
    }

    public float asFloat() {
        return NumberConversions.toFloat(data);
    }

    public short asShort() {
        return NumberConversions.toShort(data);
    }

    public long asLong() {
        return NumberConversions.toLong(data);
    }

    public ItemTag asCompound() {
        return (ItemTag) data;
    }

    public ItemTagList asList() {
        if (data instanceof ItemTagList) {
            return (ItemTagList) data;
        }
        return ItemTagList.of(data);
    }

    public ItemTagType getType() {
        return type;
    }

    public static ItemTagData toNBT(Object obj) {
        if (obj instanceof ItemTagData) {
            return (ItemTagData) obj;
        } else if (obj instanceof String) {
            if (SHORT_PATTERN.matcher(obj.toString()).matches()) {
                return toNBT(Short.valueOf(obj.toString().substring(0, obj.toString().length() - 1)));
            } else {
                return new ItemTagData((String) obj);
            }
        } else if (obj instanceof Integer) {
            return new ItemTagData((int) obj);
        } else if (obj instanceof Double) {
            return new ItemTagData((double) obj);
        } else if (obj instanceof Float) {
            return new ItemTagData((float) obj);
        } else if (obj instanceof Short) {
            return new ItemTagData((short) obj);
        } else if (obj instanceof Long) {
            return new ItemTagData((long) obj);
        } else if (obj instanceof Byte) {
            return new ItemTagData((byte) obj);
        } else if (obj instanceof byte[]) {
            return new ItemTagData((byte[]) obj);
        } else if (obj instanceof int[]) {
            return new ItemTagData((int[]) obj);
        } else if (obj instanceof List) {
            return translateList(new ItemTagList(), (List) obj);
        } else if (obj instanceof Map) {
            ItemTag itemTag = new ItemTag();
            ((Map) obj).forEach((key, value) -> itemTag.put(key.toString(), toNBT(value)));
            return itemTag;
        } else if (obj instanceof ConfigurationSection) {
            ItemTag itemTag = new ItemTag();
            ((ConfigurationSection) obj).getValues(false).forEach((key, value) -> itemTag.put(key, toNBT(value)));
            return itemTag;
        }
        return new ItemTagData("Not supported: " + obj);
    }

    public static ItemTagList translateList(ItemTagList itemTagListBase, List list) {
        for (Object obj : list) {
            ItemTagData base = toNBT(obj);
            if (base == null) {
                IOKt.warning("Not supported: " + obj + " [" + obj.getClass().getSimpleName() + "]");
                continue;
            }
            itemTagListBase.add(base);
        }
        return itemTagListBase;
    }

    public static ItemTag translateSection(ItemTag nbt, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            Object obj = section.get(key);
            ItemTagData base;
            if (obj instanceof ConfigurationSection) {
                base = translateSection(new ItemTag(), section.getConfigurationSection(key));
            } else if ((base = toNBT(obj)) == null) {
                IOKt.warning("Invalid Type: " + obj + " [" + obj.getClass().getSimpleName() + "]");
                continue;
            }
            nbt.put(key, base);
        }
        return nbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemTagData)) {
            return false;
        }
        ItemTagData itemTagData = (ItemTagData) o;
        return getType() == itemTagData.getType() && Objects.equals(data, itemTagData.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), data);
    }

    @Override
    public String toString() {
        return NMS_UTILS.itemTagToString(this);
    }

    protected String copy(String text, int count) {
        return IntStream.range(0, count).mapToObj(i -> text).collect(Collectors.joining());
    }
}
