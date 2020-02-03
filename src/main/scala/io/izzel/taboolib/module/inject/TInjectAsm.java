package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.lite.SimpleVersionControl;
import io.izzel.taboolib.util.Ref;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

/**
 * @Author sky
 * @Since 2019-08-18 0:47
 */
public class TInjectAsm implements TabooLibLoader.Loader {

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            TInject annotation = declaredField.getAnnotation(TInject.class);
            if (annotation == null || annotation.asm().isEmpty()) {
                continue;
            }
            for (Object instance : TInjectHelper.getInstance(declaredField, pluginClass, plugin)) {
                try {
                    Ref.putField(instance, declaredField, SimpleVersionControl.createNMS(annotation.asm()).useCache().translate(plugin).newInstance());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
