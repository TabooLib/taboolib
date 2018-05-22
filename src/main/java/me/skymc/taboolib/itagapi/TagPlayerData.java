package me.skymc.taboolib.itagapi;

import com.ilummc.tlib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * @Author sky
 * @Since 2018-05-21 15:07
 */
public class TagPlayerData {

    private final UUID uuid;
    private String nameDisplay;
    private String prefix;
    private String suffix;

    public TagPlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.nameDisplay = player.getName();
        this.prefix = "";
        this.suffix = "";
    }

    public String getTeamHash() {
        return String.valueOf(Objects.hash(prefix));
    }

    public TagPlayerData reset() {
        this.nameDisplay = getNameOrigin();
        this.prefix = "";
        this.suffix = "";
        return this;
    }

    @Override
    public String toString() {
        return Strings.replaceWithOrder("TagPlayerData'{'uuid={0}, nameDisplay=''{1}'', prefix=''{2}'', suffix=''{3}'''}'", uuid, nameDisplay, prefix, suffix);
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
        return Bukkit.getPlayer(uuid).getName();
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
