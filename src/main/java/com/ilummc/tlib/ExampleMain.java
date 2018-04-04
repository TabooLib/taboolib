package com.ilummc.tlib;

import com.ilummc.tlib.annotations.Config;
import com.ilummc.tlib.annotations.ConfigNode;
import com.ilummc.tlib.bean.Property;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Config(name = "cfg.yml", charset = "GBK")
public class ExampleMain extends JavaPlugin {

    @ConfigNode("enableUpdate")
    private Property<Boolean> update = Property.of(false);

    @Override
    public void onEnable() {
        update.addListener(((oldVal, newVal) -> {
            Bukkit.getLogger().info("配置项 enableUpdate 的值由 " + oldVal + " 变为了 " + newVal);
            if (newVal)
                Updater.start();
            else
                Updater.stop();
        }));
    }

    private static class Updater {
        public static void start() {

        }

        public static void stop() {

        }
    }
}
