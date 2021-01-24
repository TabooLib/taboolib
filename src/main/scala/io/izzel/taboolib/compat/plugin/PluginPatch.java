package io.izzel.taboolib.compat.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.IO;
import io.izzel.taboolib.util.Pair;
import io.izzel.taboolib.util.Strings;
import io.izzel.taboolib.util.plugin.PluginUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 * @author sky
 * @since 2020-05-04 23:55
 */
public class PluginPatch {

    private static final Map<String, List<Pair<String, byte[]>>> patchMap = Maps.newHashMap();

    static {
        patch("TabooLib", "me/skymc/taboolib/database/PlayerDataManager");
        patch("TabooLib", "com/ilummc/tlib/dependency/TDependency");
        patch("TabooLib", "com/ilummc/tlib/inject/TConfigWatcher");
    }

    public static void patch(String plugin, String path) {
        try {
            patchMap.computeIfAbsent(plugin, i -> Lists.newArrayList()).add(new Pair<>(path, IO.readFully(Files.getResourceChecked(TabooLib.getPlugin(), "patch/" + path.substring(path.lastIndexOf("/") + 1) + ".class"))));
        } catch (Throwable ignore) {
        }
    }

    @TFunction.Init
    static void init() {
        List<String> p = Lists.newArrayList();
        for (Map.Entry<String, List<Pair<String, byte[]>>> entry : patchMap.entrySet()) {
            File file;
            try {
                file = PluginUtils.getPluginFile(entry.getKey());
                if (file == null) {
                    continue;
                }
            } catch (Throwable ignored) {
                continue;
            }
            boolean i = false;
            try (ZipFile zipFile = new ZipFile(file)) {
                for (Pair<String, byte[]> pair : entry.getValue()) {
                    String hash1 = Strings.hashKeyForDisk(IO.readFully(zipFile.getInputStream(zipFile.getEntry(pair.getKey() + ".class"))), "sha1");
                    String hash2 = Strings.hashKeyForDisk(pair.getValue(), "sha1");
                    if (!hash1.equals(hash2)) {
                        i = true;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (i) {
                File input = Files.folder(TabooLib.getPlugin().getDataFolder(), "patch/" + entry.getKey());
                File output = Files.file(TabooLib.getPlugin().getDataFolder(), "patch-output/" + file.getName());
                Files.fromZip(file, input);
                for (Pair<String, byte[]> pair : entry.getValue()) {
                    Files.toFile(pair.getValue(), Files.file(TabooLib.getPlugin().getDataFolder(), "patch/" + entry.getKey() + "/" + pair.getKey() + ".class"));
                }
                Files.toZipSkipDirectory(input, output);
                Files.copy(output, file);
                p.add(file.getName());
            }
        }
        if (!p.isEmpty()) {
            Files.deepDelete(Files.folder(TabooLib.getPlugin().getDataFolder(), "patch"));
            Files.deepDelete(Files.folder(TabooLib.getPlugin().getDataFolder(), "patch-output"));
            for (String name : p) {
                System.out.println("[TabooLib] The \"" + name + "\" has patched.");
            }
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                System.out.println("[TabooLib] The Server will be restart now.");
                try {
                    Thread.sleep(3000L);
                } catch (Throwable ignored) {
                }
                Bukkit.shutdown();
            } else {
                System.out.println("[TabooLib] The Server required restart now.");
            }
        }
    }

}
