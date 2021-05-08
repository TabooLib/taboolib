package io.izzel.taboolib.compat;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * TabooLib
 * io.izzel.taboolib.compat.LegacyLoader
 *
 * @author sky
 * @since 2021/5/8 8:22 上午
 */
public class LegacyLoader {

    private static boolean isLegacyLoaded = false;
    private static final List<String> legacyPlugins = new ArrayList<>();

    @TFunction.Load
    private static void load() {
        for (File file : new File("plugins").listFiles()) {
            try {
                PluginDescriptionFile description = TabooLib.getPlugin().getPluginLoader().getPluginDescription(file);
                if (description.getName().equals("TabooLib")) {
                    isLegacyLoaded = true;
                }
                if (description.getDepend().contains("TabooLib") || description.getSoftDepend().contains("TabooLib")) {
                    legacyPlugins.add(description.getName());
                }
            } catch (InvalidDescriptionException ignored) {
            }
        }
        if (legacyPlugins.size() > 0 && !isLegacyLoaded) {
            List<String> messages = TLocale.asStringList("GENERAL.LEGACY-PLUGIN-REQUIRED", String.join(", ", legacyPlugins));
            for (String message : messages) {
                Bukkit.getLogger().warning("[TabooLib] " + message);
            }
            try {
                Files.downloadFile("https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib-4.92.jar", new File("plugins/TabooLib.jar"));
            } catch (ConnectException e) {
                e.printStackTrace();
            }
        }
    }
}
