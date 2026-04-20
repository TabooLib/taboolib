package taboolib.common;

import sun.misc.Unsafe;
import taboolib.common.classloader.IsolatedClassLoader;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static taboolib.common.PrimitiveIO.t;

/**
 * @author sky
 * @since 2020-04-12 22:39
 */
public class ClassAppender {

    static MethodHandles.Lookup lookup;
    static Unsafe unsafe;
    static List<Callback> callbacks = new ArrayList<>();

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
            // 如果第二个 IMPL_LOOKUP 没有找到，提示无法加载
            if (lookup == null) {
                PrimitiveIO.warning(t(
                        "未能找到 Unsafe lookup，TabooLib 将无法正常工作。",
                        "Unsafe lookup not found, TabooLib will not work properly."
                ));
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 加载一个文件到 ClassLoader
     *
     * @param path       路径
     * @param isIsolated 是否隔离
     * @param isExternal 是否外部库（不加入 loadedClasses）
     */
    public static ClassLoader addPath(Path path, boolean isIsolated, boolean isExternal) throws Throwable {
        File file = new File(path.toUri().getPath());
        // IsolatedClassLoader
        if (isIsolated) {
            IsolatedClassLoader loader = IsolatedClassLoader.INSTANCE;
            loader.addURL(file.toURI().toURL());
            for (Callback i : callbacks) {
                i.add(loader, file, isExternal);
            }
            return loader;
        }
        ClassLoader loader = TabooLib.class.getClassLoader();
        // Application
        if (loader.getClass().getSimpleName().equals("AppClassLoader")) {
            addURL(loader, ucp(loader.getClass()), file, isExternal);
        }
        // Hybrid
        else if (loader.getClass().getName().equals("net.minecraft.launchwrapper.LaunchClassLoader")) {
            MethodHandle methodHandle = lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, java.net.URL.class));
            methodHandle.invoke(loader, file.toURI().toURL());
        }
        // Bukkit
        else {
            addURL(loader, ucp(loader), file, isExternal);
        }
        return loader;
    }

    /**
     * 获取 addPath 函数所使用的 ClassLoader（原函数为：judgeAddPathClassLoader）
     */
    public static ClassLoader getClassLoader() {
        return PrimitiveSettings.IS_ISOLATED_MODE ? IsolatedClassLoader.INSTANCE : TabooLib.class.getClassLoader();
    }

    /**
     * 判断类是否粗在
     */
    public static boolean isExists(String path) {
        try {
            Class.forName(path, false, getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static void addURL(ClassLoader loader, Field ucpField, File file, boolean isExternal) throws Throwable {
        if (ucpField == null) {
            throw new IllegalStateException("ucp field not found");
        }
        if (lookup == null) {
            throw new IllegalStateException("lookup not found");
        }
        Object ucp = unsafe.getObject(loader, unsafe.objectFieldOffset(ucpField));
        try {
            MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(void.class, URL.class));
            methodHandle.invoke(ucp, file.toURI().toURL());
            for (Callback i : callbacks) {
                i.add(loader, file, isExternal);
            }
        } catch (NoSuchMethodError e) {
            throw new IllegalStateException("Unsupported (classloader: " + loader.getClass().getName() + ", ucp: " + ucp.getClass().getName() + ")", e);
        }
    }

    private static Field ucp(ClassLoader loader) {
        try {
            return URLClassLoader.class.getDeclaredField("ucp");
        } catch (NoSuchFieldError | NoSuchFieldException ignored) {
            return ucp(loader.getClass());
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

    public static void registerCallback(Callback callback) {
        callbacks.add(callback);
    }

    public interface Callback {

        void add(ClassLoader loader, File file, boolean isExternal);
    }
}
