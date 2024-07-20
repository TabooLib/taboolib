package taboolib.module.nms;

import taboolib.platform.BukkitPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TabooLib
 * taboolib.module.nms.PaperReflect
 *
 * @author 坏黑
 * @since 2024/7/20 16:07
 */
@SuppressWarnings("ALL")
public class LightReflection {

    private static final String PAPER_REFLECTION_HOLDER = "io.papermc.paper.pluginremap.reflect.PaperReflectionHolder";
    private static final String PAPER_REFLECTION_REMAPPER = "io.papermc.paper.pluginremap.reflect.ReflectionRemapper";

    private static Class<?> paperReflectionHolder;
    private static Method forName;

    static {
        try {
            paperReflectionHolder = (Class<Class<?>>) Class.forName(PAPER_REFLECTION_HOLDER);
            forName = paperReflectionHolder.getDeclaredMethod("forName", String.class, boolean.class, ClassLoader.class);
            forName.setAccessible(true);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 在 Paper 1.20.6+ 采用了 Mojang Mapping，但同时也提供了动态 remap 以向下兼容。
     * 由于 TabooLib 采用外部加载，无法直接被 Paper 接管，因此需要手动调用相关函数。
     */
    public static Class<?> forName(String name) throws ClassNotFoundException {
        return forName(name, true, BukkitPlugin.class.getClassLoader());
    }

    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        if (forName != null) {
            try {
                return (Class<?>) forName.invoke(null, name, initialize, loader);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Class.forName(name, initialize, loader);
        }
    }
}
