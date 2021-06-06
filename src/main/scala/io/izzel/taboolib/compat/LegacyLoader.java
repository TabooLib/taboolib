package io.izzel.taboolib.compat;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

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
    private static final List<String> updatePlugins = new ArrayList<>();

    @TFunction.Init
    private static void load() {
        Files.deepDelete(Files.folder(TabooLib.getPlugin().getDataFolder(), "legacy"));
        Files.deepDelete(Files.folder(TabooLib.getPlugin().getDataFolder(), "legacy-output"));
        for (File file : new File("plugins").listFiles()) {
            if (file.getName().endsWith(".jar")) {
                // 文件为空则删除
                if (file.length() == 0) {
                    file.delete();
                    continue;
                }
                try {
                    PluginDescriptionFile description = TabooLib.getPlugin().getPluginLoader().getPluginDescription(file);
                    if (description.getName().equals("TabooLib")) {
                        isLegacyLoaded = true;
                    }
                    if (description.getDepend().contains("TabooLib") || description.getSoftDepend().contains("TabooLib")) {
                        legacyPlugins.add(description.getName());
                    }
                    boolean delete = false;
                    try (ZipFile zipFile = new ZipFile(file)) {
                        if (zipFile.getEntry("kotlin/KotlinVersion.class") != null) {
                            System.out.println("[TabooLib] Updating " + description.getName() + "...");
                            delete = true;
                            // 复制备份
                            Files.copy(file, new File("plugins/" + description.getName() + ".bak"));
                            // 修改插件
                            File input = Files.folder(TabooLib.getPlugin().getDataFolder(), "legacy/" + description.getName());
                            File output = Files.file(TabooLib.getPlugin().getDataFolder(), "legacy-output/" + file.getName());
                            Files.fromZip(file, input);
                            // 删除 Kotlin 库
                            Files.deepDelete(new File(input, "kotlin"));
                            Files.toZipSkipDirectory(input, output);
                            // 复制插件
                            Files.copy(output, new File("plugins/(TabooLib) " + description.getName() + ".jar"));
                            updatePlugins.add(description.getName());
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    if (delete) {
                        Files.toFile(new byte[0], file);
                        Files.deepDelete(file);
                    }
                } catch (InvalidDescriptionException ignored) {
                }
            }
        }
        boolean restart = false;
        if (legacyPlugins.size() > 0 && !isLegacyLoaded) {
            Bukkit.getLogger().warning("[TabooLib] " + String.join(", ", legacyPlugins) + " depend legacy version (4.X) of TabooLib.");
            Bukkit.getLogger().warning("[TabooLib] The TabooLib 5.0+ will stop being compatible with its when Minecraft 1.17 is released, update as soon as possible.");
            try {
                Files.downloadFile("https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib-4.92.jar", new File("plugins/TabooLib.jar"));
            } catch (ConnectException e) {
                e.printStackTrace();
            }
            restart = true;
        }
        if (updatePlugins.size() > 0 || restart) {
            Bukkit.getLogger().warning("[TabooLib] The Server will be restart now.");
            try {
                Thread.sleep(3000L);
            } catch (Throwable ignored) {
            }
            Bukkit.shutdown();
        }
    }
}
