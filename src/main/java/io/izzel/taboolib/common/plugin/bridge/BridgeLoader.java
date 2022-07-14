package io.izzel.taboolib.common.plugin.bridge;

import org.bukkit.Bukkit;

/**
 * @author 坏黑
 * @since 2019-07-09 17:43
 */
public class BridgeLoader extends ClassLoader {

    private static ClassLoader pluginClassLoader;

    private BridgeLoader() {
        super(BridgeLoader.class.getClassLoader());
        try {
            pluginClassLoader = Bukkit.getPluginManager().getPlugins()[0].getClass().getClassLoader();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name, false, pluginClassLoader);
//            MethodHandle methodHandle = Ref.lookup().findVirtual(ClassLoader.class, "findClass", MethodType.methodType(Class.class, String.class));
//            Object o = methodHandle.invoke(pluginClassLoader, name);
//            if (o != null) {
//                return (Class<?>) o;
//            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return super.findClass(name);
    }

    public static Class<?> createNewClass(String name, byte[] arr) {
        return new BridgeLoader().defineClass(name, arr, 0, arr.length, BridgeLoader.class.getProtectionDomain());
    }
}
