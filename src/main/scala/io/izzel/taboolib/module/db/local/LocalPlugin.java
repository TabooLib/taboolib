package io.izzel.taboolib.module.db.local;

import com.google.common.collect.Maps;
import com.sun.istack.internal.NotNull;
import io.izzel.taboolib.util.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-07-06 16:55
 */
public class LocalPlugin {

    private String name;
    private Map<String, FileConfiguration> files = Maps.newConcurrentMap();

    public LocalPlugin(String name) {
        this.name = name;
    }

    @NotNull
    public FileConfiguration get(String name) {
        return files.computeIfAbsent(fixName(name), n -> Files.load(toFile(n)));
    }

    @NotNull
    public FileConfiguration getFile(String name) {
        return files.getOrDefault(fixName(name), new YamlConfiguration());
    }

    @NotNull
    public FileConfiguration addFile(String name) {
        FileConfiguration file = Files.load(toFile(name));
        files.put(fixName(name), file);
        return file;
    }

    @Nullable
    public FileConfiguration clearFile(String name) {
        return files.remove(fixName(name));
    }

    public void clearFiles() {
        files.clear();
    }

    public void saveFiles() {
        files.forEach((name, file) -> {
            try {
                file.save(toFile(name));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private File toFile(String name) {
        return Files.file("plugins/" + this.name + "/" + fixName(name));
    }

    private String fixName(String name) {
        return name.endsWith(".yml") ? name : name + ".yml";
    }

    public String getName() {
        return name;
    }

    public Map<String, FileConfiguration> getFiles() {
        return files;
    }
}
