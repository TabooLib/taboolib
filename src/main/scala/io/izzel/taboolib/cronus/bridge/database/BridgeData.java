package io.izzel.taboolib.cronus.bridge.database;

import com.google.common.collect.Maps;
import io.izzel.taboolib.module.config.TConfigMigrate;
import io.izzel.taboolib.module.db.local.SecuredFile;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.Set;

/**
 * @Author sky
 * @Since 2020-07-03 17:11
 */
public class BridgeData {

    private final String id;
    private final Map<String, Object> update = Maps.newHashMap();
    private FileConfiguration data = new SecuredFile();
    private boolean checked = false;
    private long time = System.currentTimeMillis();

    public BridgeData(String id) {
        this.id = id;
    }

    public BridgeData(String id, FileConfiguration data) {
        this.id = id;
        this.data = data;
        update();
    }

    public BridgeData(String id, Set<Map.Entry<String, Object>> input) {
        this.id = id;
        parse(input, "");
        update();
    }

    public void update() {
        time = System.currentTimeMillis();
        update.clear();
        update.putAll(TConfigMigrate.toMap(data));
    }

    public void parse(Set<Map.Entry<String, Object>> input, String node) {
        for (Map.Entry<String, Object> pair : input) {
            if (pair.getValue() instanceof Document) {
                parse(((Document) pair.getValue()).entrySet(), node + "." + pair.getKey() + ".");
            } else {
                this.data.set(node + pair.getKey(), pair.getValue());
            }
        }
    }

    public String getId() {
        return id;
    }

    public FileConfiguration getData() {
        time = System.currentTimeMillis();
        return data;
    }

    public Map<String, Object> getUpdate() {
        return update;
    }

    public boolean isChecked() {
        return checked;
    }

    public BridgeData setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
