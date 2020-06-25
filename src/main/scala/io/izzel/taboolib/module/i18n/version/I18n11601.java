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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * https://launchermeta.mojang.com/mc/game/version_manifest.json
 *
 * @Author sky
 * @Since 2020-04-04 19:51
 */
public class I18n11601 extends I18nBase {

    public static final I18n11601 INSTANCE = new I18n11601();

    // 1。16.1
    public static final String[][] LOCALE = {
            {
                    "zh_cn", "207c12360216c9222878b8c2b6ac0660d8a7f7bb"
            },
            {
                    "zh_tw", "e3721a0efb6077faa7d5525c189a36485ce1366c"
            },
            {
                    "en_gb", "7697b4bfb780270caad749f38abb9007fc1bd976",
            }
    };

    private final File folder = new File(TabooLib.getPlugin().getDataFolder(), "simpleI18n/v2/1.16.1");
    private final Map<String, JsonObject> cache = Maps.newHashMap();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void init() {
        executor.submit(() -> {
            if (folder.exists() && folder.isDirectory()) {
                load();
            } else {
                System.out.println("[TabooLib] Loading Assets...");
                long time = System.currentTimeMillis();
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
        });
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
