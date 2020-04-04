package io.izzel.taboolib.module.i18n.version;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.i18n.I18nBase;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.util.Files;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * @Author sky
 * @Since 2020-04-04 19:51
 */
public class I18n20w14a extends I18nBase {

    public static final I18n20w14a INSTANCE = new I18n20w14a();

    // 20w14a
    public static final String[][] LOCALE = {
            {
                    "zh_cn", "5797b76621b4b335527cda6f903a1bb3d9b7ffa3"
            },
            {
                    "zh_tw", "71be70e62f4cf7e98c6bcb32d57d7ef66614853b"
            },
            {
                    "en_gb", "0f679e66f50f1dea4749cac3882cf5c6857bbfc3",
            }
    };

    private File folder = new File(TabooLib.getPlugin().getDataFolder(), "simpleI18n/v2/20w14a");
    private Map<String, JsonObject> cache = Maps.newHashMap();

    @Override
    public void init() {
        if (folder.exists() && folder.isDirectory()) {
            load();
        } else {
            long time = System.currentTimeMillis();
            System.out.println("[TabooLib] Loading Assets...");
            try {
                for (String[] locale : LOCALE) {
                    Files.toFile(Files.readFromURL("https://resources.download.minecraft.net/" + locale[1].substring(0, 2) + "/" + locale[1], StandardCharsets.UTF_8, "{}"), Files.file(folder, locale[0]));
                }
                load();
                System.out.println("[TabooLib] Loading Successfully. (" + (System.currentTimeMillis() - time + "ms)"));
            } catch (Throwable ignored) {
                System.out.println("[TabooLib] Loading Failed. (" + (System.currentTimeMillis() - time + "ms)"));
            }
        }
    }

    @Override
    public String getName(Player player, Entity entity) {
        JsonObject locale = cache.get(player == null ? "zh_cn" : player.getLocale());
        if (locale == null) {
            locale = cache.get("en_gb");
        }
        if (locale == null) {
            return "[ERROR LOCALE]";
        }
        JsonElement element = locale.get(NMS.handle().getName(entity));
        return element == null ? entity.getName() : element.getAsString();
    }

    @Override
    public String getName(Player player, ItemStack itemStack) {
        JsonObject locale = cache.get(player == null ? "zh_cn" : player.getLocale());
        if (locale == null) {
            locale = cache.get("en_gb");
        }
        if (locale == null) {
            return "[ERROR LOCALE]";
        }
        JsonElement element = locale.get(NMS.handle().getName(itemStack));
        return element == null ? itemStack.getType().name().toLowerCase().replace("_", "") : element.getAsString();
    }

    public void load() {
        Arrays.stream(folder.listFiles()).forEach(listFile -> cache.put(listFile.getName(), new JsonParser().parse(Files.readFromFile(listFile)).getAsJsonObject()));
    }
}
