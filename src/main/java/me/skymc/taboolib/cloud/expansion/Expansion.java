package me.skymc.taboolib.cloud.expansion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ilummc.eagletdl.EagletTask;
import com.ilummc.eagletdl.ProgressEvent;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.cloud.TCloudLoader;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.string.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.util.NumberConversions;

import java.io.File;
import java.util.stream.IntStream;

/**
 * @Author sky
 * @Since 2018-09-30 16:39
 */
public class Expansion {

    private final String name;
    private final String[] author;
    private final String description;
    private final String[] detail;
    private final String version;
    private final String lastUpdate;
    private final String lastUpdateNote;
    private final String link;
    private final ExpansionType type;

    public Expansion(String name, String[] author, String description, String[] detail, String version, String lastUpdate, String lastUpdateNote, String link, ExpansionType type) {
        this.name = name;
        this.author = author;
        this.description = description;
        this.detail = detail;
        this.version = version;
        this.lastUpdate = lastUpdate;
        this.lastUpdateNote = lastUpdateNote;
        this.link = link;
        this.type = type;
    }

    public static Expansion unSerialize(ExpansionType type, String name, JsonObject object) {
        String[] author = object.get("author").isJsonArray() ? toArray(object.get("author").getAsJsonArray()) : ArrayUtils.asArray(object.get("author").getAsString());
        String description = object.get("description").getAsString();
        String[] detail = object.get("detail").isJsonArray() ? toArray(object.get("detail").getAsJsonArray()) : ArrayUtils.asArray(object.get("detail").getAsString());
        String version = object.get("version").getAsString();
        String lastUpdate = object.get("last_update").getAsString();
        String lastUpdateNote = object.get("last_update_note").getAsString();
        String link = object.get("link").getAsString();
        return new Expansion(name, author, description, detail, version, lastUpdate, lastUpdateNote, link, type);
    }

    public static String[] toArray(JsonArray json) {
        return IntStream.range(0, json.size()).mapToObj(i -> json.get(i).getAsString()).toArray(String[]::new);
    }

    public String getName() {
        return name;
    }

    public String[] getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String[] getDetail() {
        return detail;
    }

    public String getVersion() {
        return version;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getLastUpdateNote() {
        return lastUpdateNote;
    }

    public String getLink() {
        return link;
    }

    public ExpansionType getType() {
        return type;
    }

    public File getFile() {
        return type == ExpansionType.INTERNAL ? new File(TCloudLoader.getExpansionInternalFolder(), "[TCLOUD] " + name + ".jar") : new File("plugins/[TCLOUD] " + name + ".jar");
    }

    public boolean canUpdate() {
        if (!TCloudLoader.isExpansionExists(this)) {
            return false;
        }
        return type == ExpansionType.PLUGIN && NumberConversions.toDouble(Bukkit.getPluginManager().getPlugin(name).getDescription().getVersion()) < NumberConversions.toDouble(version);
    }
}
