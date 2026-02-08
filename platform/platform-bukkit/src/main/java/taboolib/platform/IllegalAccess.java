package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.tabooproject.reflex.UnsafeAccess;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;

import java.lang.reflect.Field;
import java.util.Set;

public class IllegalAccess {

    /**
     * 移除 Spigot 的访问警告：
     * Loaded class {0} from {1} which is not a depend, softdepend or loadbefore of this plugin
     * <p>
     * 由于 Kotlin 可能来自共享库，这个警告十分二逼
     */
    @SuppressWarnings("DataFlowIssue")
    public static void inject() {
        PrimitiveIO.debug("已屏蔽 IllegalAccess 警告，用时 {0} 毫秒。", TabooLib.execution(() -> {
            try {
                ClassLoader classLoader = BukkitPlugin.class.getClassLoader();
                // 获取插件描述文件
                Field descriptionField = classLoader.getClass().getDeclaredField("description");
                PluginDescriptionFile description = UnsafeAccess.INSTANCE.get(classLoader, descriptionField);
                // 获取 seenIllegalAccess 容器
                Field seenIllegalAccessField = classLoader.getClass().getDeclaredField("seenIllegalAccess");
                Set<String> accessSelf = UnsafeAccess.INSTANCE.get(classLoader, seenIllegalAccessField);
                // 获取其他插件
                for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (plugin.getClass().getName().endsWith("platform.BukkitPlugin")) {
                        // 伪造双方的警告记录
                        Set<String> accessOther = UnsafeAccess.INSTANCE.get(plugin.getClass().getClassLoader(), seenIllegalAccessField);
                        accessOther.add(description.getName());
                        accessSelf.add(plugin.getName());
                    }
                }
            } catch (Throwable ignored) {
            }
        }));
    }
}
