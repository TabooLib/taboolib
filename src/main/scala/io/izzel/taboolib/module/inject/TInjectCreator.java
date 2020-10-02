package io.izzel.taboolib.module.inject;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author sky
 * @Since 2019-08-17 22:50
 */
public class TInjectCreator implements TabooLibLoader.Loader {

    private static final Map<ClassData, InstanceData> instanceMap = Maps.newHashMap();

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public void preLoad(Plugin plugin, Class<?> pluginClass) {
        instance(plugin, pluginClass, TInject.State.LOADING);
        eval(pluginClass, TInjectHelper.State.PRE);
    }

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        instance(plugin, pluginClass, TInject.State.STARTING);
        eval(pluginClass, TInjectHelper.State.POST);
    }

    @Override
    public void activeLoad(Plugin plugin, Class<?> pluginClass) {
        instance(plugin, pluginClass, TInject.State.ACTIVATED);
        eval(pluginClass, TInjectHelper.State.ACTIVE);
    }

    @Override
    public void unload(Plugin plugin, Class<?> pluginClass) {
        eval(pluginClass, TInjectHelper.State.CANCEL);
    }

    public void instance(Plugin plugin, Class<?> loadClass, TInject.State state) {
        for (Field declaredField : loadClass.getDeclaredFields()) {
            TInject annotation = declaredField.getAnnotation(TInject.class);
            if (annotation == null || annotation.state() != state) {
                continue;
            }
            List<Object> instance = TInjectHelper.getInstance(declaredField, loadClass, plugin);
            if (instance.isEmpty()) {
                continue;
            }
            try {
                InstanceData instanceData = new InstanceData(declaredField.getType().newInstance(), annotation);
                Ref.putField(instance, declaredField, instanceData.getInstance());
                instanceMap.put(new ClassData(loadClass, declaredField.getType()), instanceData);
            } catch (Throwable t) {
                TLogger.getGlobalLogger().error(declaredField.getName() + " instantiation failed: " + t.getMessage());
            }
        }
    }

    public void eval(Class<?> loadClass, TInjectHelper.State state) {
        for (Map.Entry<ClassData, InstanceData> entry : instanceMap.entrySet()) {
            if (entry.getKey().getParent().equals(loadClass) && !TInjectHelper.fromState(entry.getValue().getInject(), state).isEmpty()) {
                try {
                    Reflection.invokeMethod(entry.getValue().getInstance(), TInjectHelper.fromState(entry.getValue().getInject(), state));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public static Map<ClassData, InstanceData> getInstanceMap() {
        return instanceMap;
    }

    /**
     * 用于防止多个类使用同一个类型
     */
    public static class ClassData {

        private final Class<?> parent;
        private final Class<?> type;

        public ClassData(Class<?> parent, Class<?> type) {
            this.parent = parent;
            this.type = type;
        }

        public Class<?> getParent() {
            return parent;
        }

        public Class<?> getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ClassData)) {
                return false;
            }
            ClassData classData = (ClassData) o;
            return Objects.equals(getParent(), classData.getParent()) && Objects.equals(getType(), classData.getType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getParent(), getType());
        }
    }

    public static class InstanceData {

        private final Object instance;
        private final TInject inject;

        public InstanceData(Object instance, TInject inject) {
            this.instance = instance;
            this.inject = inject;
        }

        public Object getInstance() {
            return instance;
        }

        public TInject getInject() {
            return inject;
        }
    }
}
