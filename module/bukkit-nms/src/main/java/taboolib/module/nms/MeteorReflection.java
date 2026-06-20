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
public class MeteorReflection {

    private static final String PAPER_REFLECTION_HOLDER = "io.papermc.paper.pluginremap.reflect.PaperReflectionHolder";
    private static final String PAPER_REFLECTION_REMAPPER = "io.papermc.paper.pluginremap.reflect.ReflectionRemapper";

    private static Class<?> paperReflectionHolder;
    private static Method forName;

    private static String minecraftVersion = "UNKNOWN";

    private static boolean isMojangMapping = false;

    static {
        try {
            Class.forName("net.minecraft.core.MappedRegistry");
            isMojangMapping = true;
        } catch (Throwable ignored) {
        }
        try {
            paperReflectionHolder = (Class<Class<?>>) Class.forName(PAPER_REFLECTION_HOLDER);
            forName = paperReflectionHolder.getDeclaredMethod("forName", String.class, boolean.class, ClassLoader.class);
            forName.setAccessible(true);
        } catch (Throwable ignored) {
        }
        // 简单判断
        final Object server = getBukkitServerOrNull();
        if (server != null) {
            final String obcPackage = server.getClass().getName();
            if (obcPackage.startsWith("org.bukkit.craftbukkit.v1_")) {
                minecraftVersion = obcPackage.split("\\.")[3];
            }
        }
    }

    private static Object getBukkitServerOrNull() {
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit", false, MeteorReflection.class.getClassLoader());
            return bukkit.getMethod("getServer").invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isBukkitAvailable() {
        return getBukkitServerOrNull() != null;
    }

    public static boolean isMojangMapping() {
        return isMojangMapping;
    }

    /**
     * 由 "extra.properties" 启动，依赖加载后迅速接管 TabooLib 类查找器
     */
    static void init() {
        if (!isBukkitAvailable()) {
            PrimitiveIO.debug("PaperClassFinder 未启用：当前环境不存在 Bukkit。");
            return;
        }
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
            /**
             * 不是 mojang mapping 的版本有：
             * - 1.21.11 及以下的全部 spigot
             * - 1.20.5 以下的 paper
             * 这些版本无法从 mojang mapping 查找 spigot deobf
             * 其余版本：
             * - 这些版本均无需从 mojang mapping 查找 spigot deobf
             * - 26.1 及以上不能查找 spigot deobf
             * - 1.20.6 及以上，paper reflection holder 提供了从 spigot deobf 查找 mojang deobf，无需提供
             *
             * 综上所述，只需要为非 mojang mapping 版本提供从 mojang mapping 查找 spigot deobf 的功能即可.
             */
            if (!isMojangMapping) {
                // 为不带版本的 obc 包名添加版本号
                if (!"UNKNOWN".equals(minecraftVersion) && name.startsWith("org.bukkit.craftbukkit") && !name.startsWith("org.bukkit.craftbukkit.v1")) {
                    name = name.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + minecraftVersion);
                }
                // 处理 nms 类
                if (name.startsWith("net.minecraft")) {
                    final String translatedName = name.replace("\\.", "/");
                    name = MinecraftVersion.INSTANCE.getPaperMapping().getClassMapMojangToSpigot().getOrDefault(translatedName, translatedName).replace("/", "\\.");
                }
            }
            return Class.forName(name, initialize, loader);
        }
    }
}
