package io.izzel.taboolib.module.dependency;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.util.Strings;
import org.bukkit.plugin.Plugin;

/**
 * @author Izzel_Aliz
 */
public class TDependencyInjector {

    public static Dependency[] getDependencies(Class<?> clazz) {
        Dependency[] dependencies = new Dependency[0];
        Dependencies d = clazz.getAnnotation(Dependencies.class);
        if (d != null) {
            dependencies = d.value();
        }
        Dependency d2 = clazz.getAnnotation(Dependency.class);
        if (d2 != null) {
            dependencies = new Dependency[] {d2};
        }
        return dependencies;
    }

    public static void inject(Plugin plugin, Class<?> clazz) {
        inject(plugin.getName(), clazz);
    }

    public static void inject(String name, Class<?> clazz) {
        for (Dependency dependency : getDependencies(clazz)) {
            if (dependency.type() == Dependency.Type.LIBRARY) {
                if (TDependency.requestLib(dependency.maven(), dependency.mavenRepo(), dependency.url())) {
                    TabooLibAPI.debug("  Loaded " + String.join(":", dependency.maven()) + " (" + name + ")");
                } else {
                    TabooLib.getLogger().warn(Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString("DEPENDENCY-LOAD-FAIL"), name, String.join(":", dependency.maven())));
                }
            }
        }
    }
}
