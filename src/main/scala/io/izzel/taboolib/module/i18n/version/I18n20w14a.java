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
import org.jetbrains.annotations.NotNull;

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

    private final File folder = new File(TabooLib.getPlugin().getDataFolder(), "simpleI18n/v2/20w14a");
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
    public @NotNull String getName(Player player, @NotNull Entity entity) {
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
    public @NotNull String getName(Player player, @NotNull ItemStack itemStack) {
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
        Arrays.stream(Files.listFile(folder)).forEach(listFile -> cache.put(listFile.getName(), new JsonParser().parse(Files.readFromFile(listFile)).getAsJsonObject()));
    }
}
