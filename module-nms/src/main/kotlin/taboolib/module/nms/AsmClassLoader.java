package taboolib.module.nms;

public class AsmClassLoader extends ClassLoader {

    private static final class AsmClassLoaderHolder {

        private static final AsmClassLoader INSTANCE = new AsmClassLoader();
    }

    public static AsmClassLoader getInstance() {
        return AsmClassLoaderHolder.INSTANCE;
    }

    private AsmClassLoader() {
        super(AsmClassLoader.class.getClassLoader());
    }

    public static Class<?> createNewClass(String name, byte[] arr) {
        return getInstance().defineClass(name, arr, 0, arr.length, AsmClassLoader.class.getProtectionDomain());
    }
}
