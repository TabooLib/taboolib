package io.izzel.taboolib.module.ipc;

public class MessageBlock {

    private final MemoryMappedFile file;
    private final long baseOffset;
    private final long size;

    public MessageBlock(MemoryMappedFile file, long baseOffset, long size) {
        this.file = file;
        this.baseOffset = baseOffset;
        this.size = size;
    }

    public void reset() {
        int id = getId();
        file.setMemory(baseOffset, size, (byte) 0x00);
        putInt(0, id);
    }

    public int getId() {
        return getInt(0);
    }

    public long getTimestamp() {
        return getLong(4);
    }

    public void updateTimestamp() {
        putLong(4, System.currentTimeMillis());
    }

    public long getSize() {
        return size;
    }

    public long getPayloadSize() {
        return size - 64;
    }

    public long getAddress() {
        return file.getAddress() + baseOffset;
    }

    public byte getByte(long pos) {
        return file.getByte(mapAddress(pos));
    }

    public byte getByteVolatile(long pos) {
        return file.getByteVolatile(mapAddress(pos));
    }

    public int getInt(long pos) {
        return file.getInt(mapAddress(pos));
    }

    public int getIntVolatile(long pos) {
        return file.getIntVolatile(mapAddress(pos));
    }

    public long getLong(long pos) {
        return file.getLong(mapAddress(pos));
    }

    public long getLongVolatile(long pos) {
        return file.getLongVolatile(mapAddress(pos));
    }

    public void putByte(long pos, byte val) {
        file.putByte(mapAddress(pos), val);
    }

    public void putByteVolatile(long pos, byte val) {
        file.putByteVolatile(mapAddress(pos), val);
    }

    public void putInt(long pos, int val) {
        file.putInt(mapAddress(pos), val);
    }

    public void putIntVolatile(long pos, int val) {
        file.putIntVolatile(mapAddress(pos), val);
    }

    public void putLong(long pos, long val) {
        file.putLong(mapAddress(pos), val);
    }

    public void putLongVolatile(long pos, long val) {
        file.putLongVolatile(mapAddress(pos), val);
    }

    public void getBytes(long pos, byte[] data, int offset, int length) {
        file.getBytes(mapAddress(pos), data, offset, length);
    }

    public void setBytes(long pos, byte[] data, int offset, int length) {
        file.setBytes(mapAddress(pos), data, offset, length);
    }

    public boolean compareAndSwapInt(long pos, int expected, int value) {
        return file.compareAndSwapInt(mapAddress(pos), expected, value);
    }

    public boolean compareAndSwapLong(long pos, long expected, long value) {
        return file.compareAndSwapLong(mapAddress(pos), expected, value);
    }

    public int getAndAddInt(long pos, int delta) {
        return file.getAndAddInt(mapAddress(pos), delta);
    }

    public long getAndAddLong(long pos, long delta) {
        return file.getAndAddLong(mapAddress(pos), delta);
    }

    public int addAndGetInt(long pos, int delta) {
        return file.addAndGetInt(mapAddress(pos), delta);
    }

    public long addAndGetLong(long pos, long delta) {
        return file.addAndGetLong(mapAddress(pos), delta);
    }

    public long getAndSetInt(long pos, int val) {
        return file.getAndSetInt(mapAddress(pos), val);
    }

    public long getAndSetLong(long pos, long val) {
        return file.getAndSetLong(mapAddress(pos), val);
    }

    private long mapAddress(long pos) {
        if (pos < size) {
            return baseOffset + pos;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
