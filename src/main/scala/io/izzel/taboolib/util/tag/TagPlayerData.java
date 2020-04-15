package io.izzel.taboolib.util.tag;

import io.izzel.taboolib.TabooLib;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * @Author sky
 * @Since 2018-05-21 15:07
 */
public class TagPlayerData {

    private final UUID uuid;
    private final String nameOrigin;
    private String nameDisplay;
    private String prefix;
    private String suffix;
    private boolean nameAllow;

    public TagPlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.nameOrigin = player.getName();
        this.nameDisplay = player.getName();
        this.prefix = "";
        this.suffix = "";
        this.nameAllow = true;
    }

    public String getTeamHash() {
        try {
            return TabooLib.getConfig().getBoolean("TABLIST-SORT") ? String.valueOf(Objects.hash(String.valueOf(prefix))) : nameOrigin;
        } catch (Throwable ignore) {
        }
        return "null";
    }

    public TagPlayerData reset() {
        this.nameDisplay = getNameOrigin();
        this.prefix = "";
        this.suffix = "";
        this.nameAllow = true;
        return this;
    }

    @Override
    public String toString() {
        return "TagPlayerData{" +
                "uuid=" + uuid +
                ", nameOrigin='" + nameOrigin + '\'' +
                ", nameDisplay='" + nameDisplay + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", nameAllow=" + nameAllow +
                '}';
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public UUID getUUID() {
        return uuid;
    }

    public String getNameOrigin() {
        return nameOrigin;
    }

    public String getNameDisplay() {
        return nameDisplay == null ? "" : nameDisplay;
    }

    public String getPrefix() {
        return prefix == null ? "" : prefix;
    }

    public String getSuffix() {
        return suffix == null ? "" : suffix;
    }

    public boolean isNameVisibility() {
        return nameAllow;
    }

    public TagPlayerData setNameVisibility(boolean nameAllow) {
        this.nameAllow = nameAllow;
        return this;
    }

    public TagPlayerData setNameDisplay(String nameDisplay) {
        this.nameDisplay = nameDisplay.length() > 16 ? nameDisplay.substring(0, 16) : nameDisplay;
        return this;
    }

    public TagPlayerData setPrefix(String prefix) {
        this.prefix = prefix.length() > 16 ? prefix.substring(0, 16) : prefix;
        return this;
    }

    public TagPlayerData setSuffix(String suffix) {
        this.suffix = suffix.length() > 16 ? suffix.substring(0, 16) : suffix;
        return this;
    }
}
