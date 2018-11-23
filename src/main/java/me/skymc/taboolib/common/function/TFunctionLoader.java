package me.skymc.taboolib.common.function;

import com.ilummc.tlib.logger.TLogger;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.TabooLibLoader;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * @Author sky
 * @Since 2018-09-08 14:00
 */
public class TFunctionLoader implements TabooLibLoader.Loader {

    @Override
    public void load(Plugin plugin, Class<?> pluginClass) {
        if (pluginClass.isAnnotationPresent(TFunction.class)) {
            TFunction function = pluginClass.getAnnotation(TFunction.class);
            try {
                Method method = pluginClass.getDeclaredMethod(function.enable());
                method.setAccessible(true);
                method.invoke(null);
                TabooLib.debug("Function " + pluginClass.getSimpleName() + " loaded. (" + plugin.getName() + ")");
            } catch (NoSuchMethodException ignore) {
            } catch (Exception e) {
                TLogger.getGlobalLogger().warn("TFunction load Failed: " + pluginClass.getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unload(Plugin plugin, Class<?> pluginClass) {
        if (pluginClass.isAnnotationPresent(TFunction.class)) {
            TFunction function = pluginClass.getAnnotation(TFunction.class);
            try {
                Method method = pluginClass.getDeclaredMethod(function.disable());
                method.setAccessible(true);
                method.invoke(null);
                TabooLib.debug("Function " + pluginClass.getSimpleName() + " unloaded. (" + plugin.getName() + ")");
            } catch (NoSuchMethodException ignore) {
            } catch (Exception e) {
                TLogger.getGlobalLogger().warn("TFunction unload Failed: " + pluginClass.getName());
                e.printStackTrace();
            }
        }
    }
}
