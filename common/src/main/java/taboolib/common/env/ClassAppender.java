package taboolib.common.env;

import sun.misc.Unsafe;
import taboolib.common.TabooLibCommon;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * @author sky
 * @since 2020-04-12 22:39
 */
public class ClassAppender {

    static MethodHandles.Lookup lookup;
    static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable ignore) {
        }
    }

    ClassAppender() {
    }

    public static void addPath(Path path) {
        try {
            File file = new File(path.toUri().getPath());
            ClassLoader loader = TabooLibCommon.class.getClassLoader();
            // Application
            if (loader.getClass().getName().equals("jdk.internal.loader.ClassLoaders$AppClassLoader")) {
                addURL(loader, ucp(loader.getClass()), file);
            }
            // Hybrid
            else if (loader.getClass().getName().equals("net.minecraft.launchwrapper.LaunchClassLoader")) {
                MethodHandle methodHandle = lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, java.net.URL.class));
                methodHandle.invoke(loader, file.toURI().toURL());
            }
            // Bukkit
            else {
                Field ucpField;
                try {
                    ucpField = URLClassLoader.class.getDeclaredField("ucp");
                } catch (NoSuchFieldError | NoSuchFieldException e1) {
                    ucpField = ucp(loader.getClass());
                }
                addURL(loader, ucpField, file);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static boolean isExists(String path) {
        try {
            Class.forName(path, false, TabooLibCommon.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static void addURL(ClassLoader loader, Field ucpField, File file) throws Throwable {
        if (ucpField == null) {
            throw new IllegalStateException("ucp field not found");
        }
        Object ucp = unsafe.getObject(loader, unsafe.objectFieldOffset(ucpField));
        try {
            MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(void.class, URL.class));
            methodHandle.invoke(ucp, file.toURI().toURL());
        } catch (NoSuchMethodError e) {
            throw new IllegalStateException("Unsupported (classloader: " + loader.getClass().getName() + ", ucp: " + ucp.getClass().getName() + ")", e);
        }
    }

    private static Field ucp(Class<?> loader) {
        try {
            return loader.getDeclaredField("ucp");
        } catch (NoSuchFieldError | NoSuchFieldException e2) {
            Class<?> superclass = loader.getSuperclass();
            if (superclass == Object.class) {
                return null;
            }
            return ucp(superclass);
        }
    }
}
