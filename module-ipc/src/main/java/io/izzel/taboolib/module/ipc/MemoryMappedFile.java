package io.izzel.taboolib.module.ipc;

import io.izzel.taboolib.util.UNSAFE;
import sun.nio.ch.FileChannelImpl;

import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

@SuppressWarnings("restriction")
public class MemoryMappedFile {
    private static final Method mmap;
    private static final Method unmmap;
    private static final int BYTE_ARRAY_OFFSET;

    private long addr, size;

    static {
        try {
            mmap = getMethod(FileChannelImpl.class, "map0", int.class, long.class, long.class);
            unmmap = getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);
            BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getMethod(Class<?> cls, String name, Class<?>... params) throws Exception {
        Method m = cls.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    protected MemoryMappedFile(FileChannel ch, long len) throws Exception {
        this.size = len;
        this.addr = (long) mmap.invoke(ch, 1, 0L, this.size);
    }

    protected void unmap() throws Exception {
        unmmap.invoke(null, addr, this.size);
    }

    public long getAddress() {
        return addr;
    }

    public byte getByte(long pos) {
        return UNSAFE.getByte(pos + addr);
    }

    protected byte getByteVolatile(long pos) {
        return UNSAFE.getByteVolatile(null, pos + addr);
    }

    public int getInt(long pos) {
        return UNSAFE.getInt(pos + addr);
    }

    protected int getIntVolatile(long pos) {
        return UNSAFE.getIntVolatile(null, pos + addr);
    }

    public long getLong(long pos) {
        return UNSAFE.getLong(pos + addr);
    }

    protected long getLongVolatile(long pos) {
        return UNSAFE.getLongVolatile(null, pos + addr);
    }

    public void putByte(long pos, byte val) {
        UNSAFE.putByte(pos + addr, val);
    }

    protected void putByteVolatile(long pos, byte val) {
        UNSAFE.putByteVolatile(null, pos + addr, val);
    }

    public void putInt(long pos, int val) {
        UNSAFE.putInt(pos + addr, val);
    }

    protected void putIntVolatile(long pos, int val) {
        UNSAFE.putIntVolatile(null, pos + addr, val);
    }

    public void putLong(long pos, long val) {
        UNSAFE.putLong(pos + addr, val);
    }

    protected void putLongVolatile(long pos, long val) {
        UNSAFE.putLongVolatile(null, pos + addr, val);
    }

    public void getBytes(long pos, byte[] data, int offset, int length) {
        UNSAFE.copyMemory(null, pos + addr, data, BYTE_ARRAY_OFFSET + offset, length);
    }

    public void setBytes(long pos, byte[] data, int offset, int length) {
        UNSAFE.copyMemory(data, BYTE_ARRAY_OFFSET + offset, null, pos + addr, length);
    }

    protected boolean compareAndSwapInt(long pos, int expected, int value) {
        return UNSAFE.compareAndSwapInt(null, pos + addr, expected, value);
    }

    protected boolean compareAndSwapLong(long pos, long expected, long value) {
        return UNSAFE.compareAndSwapLong(null, pos + addr, expected, value);
    }

    protected int getAndAddInt(long pos, int delta) {
        return UNSAFE.getAndAddInt(null, pos + addr, delta);
    }

    protected long getAndAddLong(long pos, long delta) {
        return UNSAFE.getAndAddLong(null, pos + addr, delta);
    }

    protected int addAndGetInt(long pos, int delta) {
        return UNSAFE.getAndAddInt(null, pos + addr, delta) + delta;
    }

    protected long addAndGetLong(long pos, long delta) {
        return UNSAFE.getAndAddLong(null, pos + addr, delta) + delta;
    }

    protected long getAndSetInt(long pos, int val) {
        return UNSAFE.getAndSetInt(null, pos + addr, val);
    }

    protected long getAndSetLong(long pos, long val) {
        return UNSAFE.getAndSetLong(null, pos + addr, val);
    }

    protected void setMemory(long pos, long bytes, byte value) {
        UNSAFE.setMemory(pos + addr, bytes, value);
    }
}