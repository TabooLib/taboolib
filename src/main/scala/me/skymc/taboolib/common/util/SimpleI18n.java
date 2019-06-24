package me.skymc.taboolib.common.util;

import com.ilummc.tlib.resources.TLocaleLoader;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.function.TFunction;
import me.skymc.taboolib.common.nms.NMSHandler;
import me.skymc.taboolib.common.nms.nbt.NBTCompound;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.fileutils.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-05-22 1:16
 */
@TFunction(enable = "init")
public class SimpleI18n {

    private static FileConfiguration lang;
    private static boolean released;

    static void init() {
        File localeFile = getLocaleFile(TabooLib.instance());
        if (localeFile == null) {
            lang = new YamlConfiguration();
        } else {
            lang = ConfigUtils.load(TabooLib.instance(), localeFile);
        }
        if (lang.getInt("version") < 3 && !released) {
            released = true;
            FileUtils.deleteAllFile(new File(Main.getInst().getDataFolder(), "simpleI18n"));
            init();
        }
    }

    public static String getCustomName(Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCustomName()).orElse(getName(entity)) : getName(entity);
    }

    public static String getCustomName(ItemStack item) {
        if (item != null) {
            ItemMeta itemMeta = item.getItemMeta();
            return itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : getName(item);
        }
        return getName(item);
    }

    public static String getName(Entity entity) {
        return entity == null ? "-" : lang.getString(NMSHandler.getHandler().getName(entity).replace(".", "_"), entity.getName());
    }

    public static String getName(ItemStack item) {
        if (item == null) {
            return "-";
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof BookMeta && ((BookMeta) itemMeta).getTitle() != null) {
            return ((BookMeta) itemMeta).getTitle();
        }
        if (TabooLib.getVersionNumber() < 11100) {
            if (item.getType().name().equals("MONSTER_EGG")) {
                NBTCompound nbtCompound = NMSHandler.getHandler().loadNBT(item);
                if (nbtCompound.containsKey("EntityTag")) {
                    return lang.getString("item_monsterPlacer_name") + " " + lang.getString("entity_" + nbtCompound.get("EntityTag").asCompound().get("id").asString() + "_name");
                }
                return lang.getString("item_monsterPlacer_name");
            }
        } else if (TabooLib.getVersionNumber() < 11300) {
            if (itemMeta instanceof SpawnEggMeta) {
                String spawnEggType = lang.getString("entity_" + ((SpawnEggMeta) itemMeta).getSpawnedType().getEntityClass().getSimpleName().replace(".", "_") + "_name");
                if (spawnEggType != null) {
                    return lang.getString(NMSHandler.getHandler().getName(item).replace(".", "_"), item.getType().name().toLowerCase().replace("_", "")) + " " + spawnEggType;
                }
            }
        }
        return lang.getString(NMSHandler.getHandler().getName(item).replace(".", "_"), item.getType().name().toLowerCase().replace("_", ""));
    }

    private static void releaseLocales(Plugin plugin) {
        TLocaleLoader.getLocalePriority().stream().filter(localeName -> !new File(plugin.getDataFolder(), "simpleI18n/" + getVersion() + "/" + localeName + ".yml").exists() && plugin.getResource("simpleI18n/" + getVersion() + "/" + localeName + ".yml") != null).forEach(localeName -> plugin.saveResource("simpleI18n/" + getVersion() + "/" + localeName + ".yml", true));
    }

    private static File getLocaleFile(Plugin plugin) {
        releaseLocales(plugin);
        return TLocaleLoader.getLocalePriority().stream().map(localeName -> new File(plugin.getDataFolder(), "simpleI18n/" + getVersion() + "/" + localeName + ".yml")).filter(File::exists).findFirst().orElse(null);
    }

    private static String getVersion() {
        return TabooLib.getVersionNumber() < 11300 ? "low" : "high";
    }
}
