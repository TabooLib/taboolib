package io.izzel.taboolib.common.plugin;

import org.bukkit.plugin.java.JavaPlugin;
/**
 * @Author 坏黑
 * @Since 2019-07-05 14:13
 */
public class InternalJavaPlugin extends JavaPlugin {

    static {
        try {
            Class.forName("io.izzel.taboolib.TabooLib");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
