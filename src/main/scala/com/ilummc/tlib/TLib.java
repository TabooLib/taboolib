package com.ilummc.tlib;

import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.config.TLibConfig;
import com.ilummc.tlib.db.Pool;
import com.ilummc.tlib.inject.TConfigWatcher;
import com.ilummc.tlib.inject.TDependencyInjector;
import com.ilummc.tlib.inject.TPluginManager;
import com.ilummc.tlib.logger.TLogger;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.resources.TLocaleLoader;
import com.ilummc.tlib.util.IO;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.plugin.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;

@Dependency(type = Dependency.Type.LIBRARY, maven = "com.zaxxer:HikariCP:3.1.0")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.slf4j:slf4j-api:1.7.25")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.javalite:activejdbc:2.0")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.javalite:javalite-common:2.0")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.javalite:app-config:2.0")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.codehaus.jackson:jackson-mapper-asl:1.9.13")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.codehaus.jackson:jackson-core-asl:1.9.13")
@Dependency(type = Dependency.Type.LIBRARY, maven = "jaxen:jaxen:1.1.6")
@Dependency(type = Dependency.Type.LIBRARY, maven = "dom4j:dom4j:1.6.1")
@Dependency(type = Dependency.Type.LIBRARY, maven = "xml-apis:xml-apis:1.0.b2")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.ehcache:ehcache:3.5.2")
@Dependency(type = Dependency.Type.LIBRARY, maven = "com.h2database:h2:1.4.197")
public class TLib {

    private static TLib tLib;
    private static YamlConfiguration internalLanguage;
    private TLogger logger = new TLogger("§8[§3§lTabooLib§8][§r{1}§8] §f{2}", Main.getInst(), TLogger.FINE);
    private TLibConfig config;
    private TConfigWatcher configWatcher = new TConfigWatcher();
    private File libsFolder;

    private TLib() {
        libsFolder = new File(Main.getInst().getDataFolder(), "/libs");
        if (!libsFolder.exists()) {
            libsFolder.mkdirs();
        }
        try {
            String yamlText = new String(IO.readFully(FileUtils.getResource("lang/internal.yml")), Charset.forName("utf-8"));
            internalLanguage = new YamlConfiguration();
            internalLanguage.loadFromString(yamlText);
        } catch (IOException | InvalidConfigurationException ignored) {
        }
    }

    public static void init() {
        tLib = new TLib();

        TLocaleLoader.init();
        PlaceholderHook.init();
        TLocaleLoader.load(Main.getInst(), false);
    }

    public static void initPost() {
        TDependencyInjector.inject(Main.getInst(), TLib.getTLib());
        try {
            Pool.init();
        } catch (Throwable ignored) {
        }
    }

    public static void unload() {
        try {
            Pool.unload();
        } catch (Throwable ignored) {
        }
        tLib.getConfigWatcher().unregisterAll();
        TDependencyInjector.eject(Main.getInst(), tLib);

    }

    public static void injectPluginManager() {
        if (!tLib.isInjectEnabled() || tLib.isBlackListPluginExists()) {
            TLocale.Logger.warn("TLIB.INJECTION-DISABLED");
            Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(plugin -> plugin != Main.getInst()).forEach(plugin -> TDependencyInjector.inject(plugin, plugin));
            return;
        }
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("pluginManager");
            field.setAccessible(true);
            field.set(Bukkit.getServer(), new TPluginManager());
            TLocale.Logger.info("TLIB.INJECTION-SUCCESS");
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException ignored) {
            TLocale.Logger.error("TLIB.INJECTION-FAILED");
            Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(plugin -> !TabooLib.isTabooLib(plugin)).forEach(plugin -> TDependencyInjector.inject(plugin, plugin));
        }
    }

    public static TLib getTLib() {
        return tLib;
    }

    public static YamlConfiguration getInternalLanguage() {
        return internalLanguage;
    }

    public TLogger getLogger() {
        return logger;
    }

    public TLibConfig getConfig() {
        return config;
    }

    public TConfigWatcher getConfigWatcher() {
        return configWatcher;
    }

    public File getLibsFolder() {
        return libsFolder;
    }

    public boolean isInjectEnabled() {
        return Main.getInst().getConfig().getBoolean("PLUGIN-INJECTOR.ENABLE", true);
    }

    public boolean isBlackListPluginExists() {
        return Main.getInst().getConfig().getStringList("PLUGIN-INJECTOR.DISABLE-ON-PLUGIN-EXISTS").stream().anyMatch(PluginUtils::isPluginExists);
    }

}
