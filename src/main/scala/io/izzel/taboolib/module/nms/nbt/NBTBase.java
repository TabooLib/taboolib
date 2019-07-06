package io.izzel.taboolib.module.nms.nbt;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author 坏黑
 * @Since 2019-05-24 17:45
 */
public class NBTBase {

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
