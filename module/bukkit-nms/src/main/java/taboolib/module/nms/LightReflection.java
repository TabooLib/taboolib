package taboolib.module.nms;

import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;

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
     * 由 "extra.properties" 启动，依赖加载后迅速接管 TabooLib 类查找器
     */
    static void init() {
        TabooLib.setClassFinder(new TabooLib.ClassFinder() {

            @Override
            public Class<?> getClass(String name) throws ClassNotFoundException {
                return forName(name, true, TabooLib.class.getClassLoader());
            }

            @Override
            public Class<?> getClass(String name, boolean initialize) throws ClassNotFoundException {
                return forName(name, initialize, TabooLib.class.getClassLoader());
            }

            @Override
            public Class<?> getClass(String name, boolean initialize, ClassLoader classLoader) throws ClassNotFoundException {
                return forName(name, initialize, classLoader);
            }
        });
        PrimitiveIO.debug("PaperClassFinder 已生效。");
    }

    /**
     * 在 Paper 1.20.6+ 采用了 Mojang Mapping，但同时也提供了动态 remap 以向下兼容。
     * 由于 TabooLib 采用外部加载，无法直接被 Paper 接管，因此需要手动调用相关函数。
     */
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
