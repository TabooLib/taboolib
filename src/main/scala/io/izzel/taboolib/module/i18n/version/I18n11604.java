package io.izzel.taboolib.module.i18n.version;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.i18n.I18nBase;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.util.Files;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * https://launchermeta.mojang.com/mc/game/version_manifest.json
 *
 * @author sky
 * @since 2020-04-04 19:51
 */
public class I18n11604 extends I18nBase {

    public static final I18n11604 INSTANCE = new I18n11604();

    // 1.16.4
    public static final String[][] LOCALE = {
            {
                    "zh_cn", "340457631ac98c2349ef3318ecf9bf0fdca3a6b9"
            },
            {
                    "zh_tw", "d31469e132068029437187e1d384fcdb7babd52d"
            },
            {
                    "en_gb", "bdd551782a0ef71c2b04537fc9cad75abbbf7cba",
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

    @Override
    public @NotNull String getName(@Nullable Player player, @NotNull Enchantment enchantment) {
        JsonObject locale = cache.get(player == null ? "zh_cn" : player.getLocale());
        if (locale == null) {
            locale = cache.get("en_gb");
        }
        if (locale == null) {
            return "[ERROR LOCALE]";
        }
        JsonElement element = locale.get(NMS.handle().getEnchantmentKey(enchantment));
        return element == null ? enchantment.getName() : element.getAsString();
    }

    @Override
    public @NotNull String getName(@Nullable Player player, @NotNull PotionEffectType potionEffectType) {
        JsonObject locale = cache.get(player == null ? "zh_cn" : player.getLocale());
        if (locale == null) {
            locale = cache.get("en_gb");
        }
        if (locale == null) {
            return "[ERROR LOCALE]";
        }
        JsonElement element = locale.get(NMS.handle().getPotionEffectTypeKey(potionEffectType));
        return element == null ? potionEffectType.getName() : element.getAsString();
    }

    public void load() {
        Arrays.stream(Files.listFile(folder)).forEach(listFile -> cache.put(listFile.getName(), new JsonParser().parse(Objects.requireNonNull(Files.readFromFile(listFile))).getAsJsonObject()));
    }
}
