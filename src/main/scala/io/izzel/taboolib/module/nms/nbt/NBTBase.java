package io.izzel.taboolib.module.nms.nbt;

import io.izzel.taboolib.TabooLib;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @Author 坏黑
 * @Since 2019-05-24 17:45
 */
public class NBTBase {

    protected static final Pattern SHORT_PATTERN = Pattern.compile("\\d+s");
    protected NBTType type;
    protected Object data;

    public NBTBase(String data) {
        this.type = NBTType.STRING;
        this.data = data;
    }

    public NBTBase(byte data) {
        this.type = NBTType.BYTE;
        this.data = data;
    }

    public NBTBase(byte[] data) {
        this.type = NBTType.BYTE_ARRAY;
        this.data = data;
    }

    public NBTBase(int data) {
        this.type = NBTType.INT;
        this.data = data;
    }

    public NBTBase(int[] data) {
        this.type = NBTType.INT_ARRAY;
        this.data = data;
    }

    public NBTBase(double data) {
        this.type = NBTType.DOUBLE;
        this.data = data;
    }

    public NBTBase(float data) {
        this.type = NBTType.FLOAT;
        this.data = data;
    }

    public NBTBase(short data) {
        this.type = NBTType.SHORT;
        this.data = data;
    }

    public NBTBase(long data) {
        this.type = NBTType.LONG;
        this.data = data;
    }

    public NBTBase(NBTCompound data) {
        this.type = NBTType.COMPOUND;
        this.data = data;
    }

    public NBTBase(NBTList data) {
        this.type = NBTType.LIST;
        this.data = data;
    }

    public String asString() {
        return (String) data;
    }

    public byte asByte() {
        return (byte) data;
    }

    public byte[] asByteArray() {
        return (byte[]) data;
    }

    public int asInt() {
        return (int) data;
    }

    public int[] asIntArray() {
        return (int[]) data;
    }

    public double asDouble() {
        return (double) data;
    }

    public float asFloat() {
        return (float) data;
    }

    public short asShort() {
        return (short) data;
    }

    public long asLong() {
        return (long) data;
    }

    public NBTCompound asCompound() {
        return (NBTCompound) data;
    }

    public NBTList asList() {
        return (NBTList) data;
    }

    public NBTType getType() {
        return type;
    }

    public static NBTBase formNBTBase(Object obj) {
        if (obj instanceof String) {
            if (SHORT_PATTERN.matcher(obj.toString()).matches()) {
                return formNBTBase(Short.valueOf(obj.toString().substring(0, obj.toString().length() - 1)));
            }
            return new NBTBase((String) obj);
        } else if (obj instanceof Integer) {
            return new NBTBase((int) obj);
        } else if (obj instanceof Double) {
            return new NBTBase((double) obj);
        } else if (obj instanceof Float) {
            return new NBTBase((float) obj);
        } else if (obj instanceof Short) {
            return new NBTBase((short) obj);
        } else if (obj instanceof Long) {
            return new NBTBase((long) obj);
        } else if (obj instanceof Byte) {
            return new NBTBase((byte) obj);
        } else if (obj instanceof List) {
            return translateList(new NBTList(), (List) obj);
        } else if (obj instanceof Map) {
            NBTCompound nbtCompound = new NBTCompound();
            ((Map) obj).forEach((key, value) -> nbtCompound.put(key.toString(), formNBTBase(value)));
            return nbtCompound;
        } else if (obj instanceof ConfigurationSection) {
            NBTCompound nbtCompound = new NBTCompound();
            ((ConfigurationSection) obj).getValues(false).forEach((key, value) -> nbtCompound.put(key, formNBTBase(value)));
            return nbtCompound;
        }
        return new NBTBase("error: " + obj);
    }

    public static NBTList translateList(NBTList nbtListBase, List list) {
        for (Object obj : list) {
            NBTBase base = formNBTBase(obj);
            if (base == null) {
                TabooLib.getLogger().warn("Invalid Type: " + obj + " [" + obj.getClass().getSimpleName() + "]");
                continue;
            }
            nbtListBase.add(base);
        }
        return nbtListBase;
    }

    public static NBTCompound translateSection(NBTCompound nbt, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            Object obj = section.get(key);
            NBTBase base;
            if (obj instanceof ConfigurationSection) {
                base = translateSection(new NBTCompound(), section.getConfigurationSection(key));
            } else if ((base = formNBTBase(obj)) == null) {
                TabooLib.getLogger().warn("Invalid Type: " + obj + " [" + obj.getClass().getSimpleName() + "]");
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
        if (!(o instanceof NBTBase)) {
            return false;
        }
        NBTBase nbtBase = (NBTBase) o;
        return getType() == nbtBase.getType() &&
                Objects.equals(data, nbtBase.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), data);
    }

    @Override
    public String toString() {
        switch (type) {
            case INT_ARRAY:
                return Arrays.toString(asIntArray());
            case BYTE_ARRAY:
                return Arrays.toString(asByteArray());
            default:
                return String.valueOf(data);
        }
    }
}
