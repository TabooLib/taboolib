package io.izzel.taboolib.util.asm;

public class AsmClassLoader extends ClassLoader {

    private static final class AsmClassLoaderHolder {
        private static AsmClassLoader instance = new AsmClassLoader();
    }

    public static AsmClassLoader getInstance() {
        return new AsmClassLoader();
    }

    private AsmClassLoader() {
        super(AsmClassLoader.class.getClassLoader());
    }

    public static Class<?> createNewClass(String name, byte[] arr) {
        return getInstance().defineClass(name, arr, 0, arr.length, AsmClassLoader.class.getProtectionDomain());
    }

}
