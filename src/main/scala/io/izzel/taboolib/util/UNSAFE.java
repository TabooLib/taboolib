package io.izzel.taboolib.util;

import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public class UNSAFE {

    private static final Unsafe unsafe;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CallerSensitive
    public static Unsafe getUnsafe() {
        return Unsafe.getUnsafe();
    }

    public static int getInt(Object o, long offset) {
        return unsafe.getInt(o, offset);
    }

    public static void putInt(Object o, long offset, int x) {
        unsafe.putInt(o, offset, x);
    }

    public static Object getObject(Object o, long offset) {
        return unsafe.getObject(o, offset);
    }

    public static void putObject(Object o, long offset, Object x) {
        unsafe.putObject(o, offset, x);
    }

    public static boolean getBoolean(Object o, long offset) {
        return unsafe.getBoolean(o, offset);
    }

    public static void putBoolean(Object o, long offset, boolean x) {
        unsafe.putBoolean(o, offset, x);
    }

    public static byte getByte(Object o, long offset) {
        return unsafe.getByte(o, offset);
    }

    public static void putByte(Object o, long offset, byte x) {
        unsafe.putByte(o, offset, x);
    }

    public static short getShort(Object o, long offset) {
        return unsafe.getShort(o, offset);
    }

    public static void putShort(Object o, long offset, short x) {
        unsafe.putShort(o, offset, x);
    }

    public static char getChar(Object o, long offset) {
        return unsafe.getChar(o, offset);
    }

    public static void putChar(Object o, long offset, char x) {
        unsafe.putChar(o, offset, x);
    }

    public static long getLong(Object o, long offset) {
        return unsafe.getLong(o, offset);
    }

    public static void putLong(Object o, long offset, long x) {
        unsafe.putLong(o, offset, x);
    }

    public static float getFloat(Object o, long offset) {
        return unsafe.getFloat(o, offset);
    }

    public static void putFloat(Object o, long offset, float x) {
        unsafe.putFloat(o, offset, x);
    }

    public static double getDouble(Object o, long offset) {
        return unsafe.getDouble(o, offset);
    }

    public static void putDouble(Object o, long offset, double x) {
        unsafe.putDouble(o, offset, x);
    }

    @Deprecated
    public static int getInt(Object o, int offset) {
        return unsafe.getInt(o, offset);
    }

    @Deprecated
    public static void putInt(Object o, int offset, int x) {
        unsafe.putInt(o, offset, x);
    }

    @Deprecated
    public static Object getObject(Object o, int offset) {
        return unsafe.getObject(o, offset);
    }

    @Deprecated
    public static void putObject(Object o, int offset, Object x) {
        unsafe.putObject(o, offset, x);
    }

    @Deprecated
    public static boolean getBoolean(Object o, int offset) {
        return unsafe.getBoolean(o, offset);
    }

    @Deprecated
    public static void putBoolean(Object o, int offset, boolean x) {
        unsafe.putBoolean(o, offset, x);
    }

    @Deprecated
    public static byte getByte(Object o, int offset) {
        return unsafe.getByte(o, offset);
    }

    @Deprecated
    public static void putByte(Object o, int offset, byte x) {
        unsafe.putByte(o, offset, x);
    }

    @Deprecated
    public static short getShort(Object o, int offset) {
        return unsafe.getShort(o, offset);
    }

    @Deprecated
    public static void putShort(Object o, int offset, short x) {
        unsafe.putShort(o, offset, x);
    }

    @Deprecated
    public static char getChar(Object o, int offset) {
        return unsafe.getChar(o, offset);
    }

    @Deprecated
    public static void putChar(Object o, int offset, char x) {
        unsafe.putChar(o, offset, x);
    }

    @Deprecated
    public static long getLong(Object o, int offset) {
        return unsafe.getLong(o, offset);
    }

    @Deprecated
    public static void putLong(Object o, int offset, long x) {
        unsafe.putLong(o, offset, x);
    }

    @Deprecated
    public static float getFloat(Object o, int offset) {
        return unsafe.getFloat(o, offset);
    }

    @Deprecated
    public static void putFloat(Object o, int offset, float x) {
        unsafe.putFloat(o, offset, x);
    }

    @Deprecated
    public static double getDouble(Object o, int offset) {
        return unsafe.getDouble(o, offset);
    }

    @Deprecated
    public static void putDouble(Object o, int offset, double x) {
        unsafe.putDouble(o, offset, x);
    }

    public static byte getByte(long address) {
        return unsafe.getByte(address);
    }

    public static void putByte(long address, byte x) {
        unsafe.putByte(address, x);
    }

    public static short getShort(long address) {
        return unsafe.getShort(address);
    }

    public static void putShort(long address, short x) {
        unsafe.putShort(address, x);
    }

    public static char getChar(long address) {
        return unsafe.getChar(address);
    }

    public static void putChar(long address, char x) {
        unsafe.putChar(address, x);
    }

    public static int getInt(long address) {
        return unsafe.getInt(address);
    }

    public static void putInt(long address, int x) {
        unsafe.putInt(address, x);
    }

    public static long getLong(long address) {
        return unsafe.getLong(address);
    }

    public static void putLong(long address, long x) {
        unsafe.putLong(address, x);
    }

    public static float getFloat(long address) {
        return unsafe.getFloat(address);
    }

    public static void putFloat(long address, float x) {
        unsafe.putFloat(address, x);
    }

    public static double getDouble(long address) {
        return unsafe.getDouble(address);
    }

    public static void putDouble(long address, double x) {
        unsafe.putDouble(address, x);
    }

    public static long getAddress(long address) {
        return unsafe.getAddress(address);
    }

    public static void putAddress(long address, long x) {
        unsafe.putAddress(address, x);
    }

    public static long allocateMemory(long bytes) {
        return unsafe.allocateMemory(bytes);
    }

    public static long reallocateMemory(long address, long bytes) {
        return unsafe.reallocateMemory(address, bytes);
    }

    public static void setMemory(Object o, long offset, long bytes, byte value) {
        unsafe.setMemory(o, offset, bytes, value);
    }

    public static void setMemory(long address, long bytes, byte value) {
        unsafe.setMemory(address, bytes, value);
    }

    public static void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
        unsafe.copyMemory(srcBase, srcOffset, destBase, destOffset, bytes);
    }

    public static void copyMemory(long srcAddress, long destAddress, long bytes) {
        unsafe.copyMemory(srcAddress, destAddress, bytes);
    }

    public static void freeMemory(long address) {
        unsafe.freeMemory(address);
    }

    @Deprecated
    public static int fieldOffset(Field f) {
        return unsafe.fieldOffset(f);
    }

    @Deprecated
    public static Object staticFieldBase(Class<?> c) {
        return unsafe.staticFieldBase(c);
    }

    public static long staticFieldOffset(Field f) {
        return unsafe.staticFieldOffset(f);
    }

    public static long objectFieldOffset(Field f) {
        return unsafe.objectFieldOffset(f);
    }

    public static Object staticFieldBase(Field f) {
        return unsafe.staticFieldBase(f);
    }

    public static boolean shouldBeInitialized(Class<?> c) {
        return unsafe.shouldBeInitialized(c);
    }

    public static void ensureClassInitialized(Class<?> c) {
        unsafe.ensureClassInitialized(c);
    }

    public static int arrayBaseOffset(Class<?> arrayClass) {
        return unsafe.arrayBaseOffset(arrayClass);
    }

    public static int arrayIndexScale(Class<?> arrayClass) {
        return unsafe.arrayIndexScale(arrayClass);
    }

    public static int addressSize() {
        return unsafe.addressSize();
    }

    public static int pageSize() {
        return unsafe.pageSize();
    }

    public static Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) {
        return unsafe.defineClass(name, b, off, len, loader, protectionDomain);
    }

    public static Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        return unsafe.defineAnonymousClass(hostClass, data, cpPatches);
    }

    public static Object allocateInstance(Class<?> cls) throws InstantiationException {
        return unsafe.allocateInstance(cls);
    }

    @Deprecated
    public static void monitorEnter(Object o) {
        unsafe.monitorEnter(o);
    }

    @Deprecated
    public static void monitorExit(Object o) {
        unsafe.monitorExit(o);
    }

    @Deprecated
    public static boolean tryMonitorEnter(Object o) {
        return unsafe.tryMonitorEnter(o);
    }

    public static void throwException(Throwable ee) {
        unsafe.throwException(ee);
    }

    public static boolean compareAndSwapObject(Object o, long offset, Object expected, Object x) {
        return unsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public static boolean compareAndSwapInt(Object o, long offset, int expected, int x) {
        return unsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public static boolean compareAndSwapLong(Object o, long offset, long expected, long x) {
        return unsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public static Object getObjectVolatile(Object o, long offset) {
        return unsafe.getObjectVolatile(o, offset);
    }

    public static void putObjectVolatile(Object o, long offset, Object x) {
        unsafe.putObjectVolatile(o, offset, x);
    }

    public static int getIntVolatile(Object o, long offset) {
        return unsafe.getIntVolatile(o, offset);
    }

    public static void putIntVolatile(Object o, long offset, int x) {
        unsafe.putIntVolatile(o, offset, x);
    }

    public static boolean getBooleanVolatile(Object o, long offset) {
        return unsafe.getBooleanVolatile(o, offset);
    }

    public static void putBooleanVolatile(Object o, long offset, boolean x) {
        unsafe.putBooleanVolatile(o, offset, x);
    }

    public static byte getByteVolatile(Object o, long offset) {
        return unsafe.getByteVolatile(o, offset);
    }

    public static void putByteVolatile(Object o, long offset, byte x) {
        unsafe.putByteVolatile(o, offset, x);
    }

    public static short getShortVolatile(Object o, long offset) {
        return unsafe.getShortVolatile(o, offset);
    }

    public static void putShortVolatile(Object o, long offset, short x) {
        unsafe.putShortVolatile(o, offset, x);
    }

    public static char getCharVolatile(Object o, long offset) {
        return unsafe.getCharVolatile(o, offset);
    }

    public static void putCharVolatile(Object o, long offset, char x) {
        unsafe.putCharVolatile(o, offset, x);
    }

    public static long getLongVolatile(Object o, long offset) {
        return unsafe.getLongVolatile(o, offset);
    }

    public static void putLongVolatile(Object o, long offset, long x) {
        unsafe.putLongVolatile(o, offset, x);
    }

    public static float getFloatVolatile(Object o, long offset) {
        return unsafe.getFloatVolatile(o, offset);
    }

    public static void putFloatVolatile(Object o, long offset, float x) {
        unsafe.putFloatVolatile(o, offset, x);
    }

    public static double getDoubleVolatile(Object o, long offset) {
        return unsafe.getDoubleVolatile(o, offset);
    }

    public static void putDoubleVolatile(Object o, long offset, double x) {
        unsafe.putDoubleVolatile(o, offset, x);
    }

    public static void putOrderedObject(Object o, long offset, Object x) {
        unsafe.putOrderedObject(o, offset, x);
    }

    public static void putOrderedInt(Object o, long offset, int x) {
        unsafe.putOrderedInt(o, offset, x);
    }

    public static void putOrderedLong(Object o, long offset, long x) {
        unsafe.putOrderedLong(o, offset, x);
    }

    public static void unpark(Object thread) {
        unsafe.unpark(thread);
    }

    public static void park(boolean isAbsolute, long time) {
        unsafe.park(isAbsolute, time);
    }

    public static int getLoadAverage(double[] loadavg, int nelems) {
        return unsafe.getLoadAverage(loadavg, nelems);
    }

    public static int getAndAddInt(Object o, long offset, int delta) {
        return unsafe.getAndAddInt(o, offset, delta);
    }

    public static long getAndAddLong(Object o, long offset, long delta) {
        return unsafe.getAndAddLong(o, offset, delta);
    }

    public static int getAndSetInt(Object o, long offset, int newValue) {
        return unsafe.getAndSetInt(o, offset, newValue);
    }

    public static long getAndSetLong(Object o, long offset, long newValue) {
        return unsafe.getAndSetLong(o, offset, newValue);
    }

    public static Object getAndSetObject(Object o, long offset, Object newValue) {
        return unsafe.getAndSetObject(o, offset, newValue);
    }

    public static void loadFence() {
        unsafe.loadFence();
    }

    public static void storeFence() {
        unsafe.storeFence();
    }

    public static void fullFence() {
        unsafe.fullFence();
    }
}
