/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package taboolib.library.xseries;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.Reflex;
import taboolib.platform.util.ItemBuilder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

/**
 * <b>SkullUtils</b> - Apply skull texture from different sources.<br>
 * Skull Meta: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/SkullMeta.html
 * Mojang API: https://wiki.vg/Mojang_API
 * <p>
 * Some websites to get custom heads:
 * <ul>
 *     <li>https://minecraft-heads.com/</li>
 * </ul>
 *
 * @author Crypto Morin
 * @version 3.1.1
 * @see XMaterial
 */
@SuppressWarnings("ConstantConditions")
public class XSkull {
    protected static final MethodHandle PROFILE_GETTER, PROFILE_SETTER;
    private static final String VALUE_PROPERTY = "{\"textures\":{\"SKIN\":{\"url\":\"";
    private static final boolean SUPPORTS_UUID = XMaterial.supports(12);
    private static final String TEXTURES = "https://textures.minecraft.net/texture/";
    //private static final Pattern BASE64 = Pattern.compile("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?");

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle profileSetter = null, profileGetter = null;

        try {
            Class<?> CraftMetaSkull = XMaterial.PLAYER_HEAD.parseItem().getItemMeta().getClass();
            Field profile = CraftMetaSkull.getDeclaredField("profile");
            profile.setAccessible(true);

            profileSetter = lookup.unreflectSetter(profile);
            profileGetter = lookup.unreflectGetter(profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        PROFILE_SETTER = profileSetter;
        PROFILE_GETTER = profileGetter;
    }

    @SuppressWarnings("deprecation")
    @NotNull
    public static ItemStack getSkull(@NotNull UUID id) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (SUPPORTS_UUID) meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        else meta.setOwner(id.toString());

        head.setItemMeta(meta);
        return head;
    }

    @SuppressWarnings("deprecation")
    @NotNull
    public static SkullMeta applySkin(@NotNull ItemMeta head, @NotNull OfflinePlayer identifier) {
        SkullMeta meta = (SkullMeta) head;
        if (SUPPORTS_UUID) {
            meta.setOwningPlayer(identifier);
        } else {
            meta.setOwner(identifier.getName());
        }
        return meta;
    }

    @NotNull
    public static SkullMeta applySkin(@NotNull ItemMeta head, @NotNull UUID identifier) {
        return applySkin(head, Bukkit.getOfflinePlayer(identifier));
    }

    @SuppressWarnings("deprecation")
    @NotNull
    public static SkullMeta applySkin(@NotNull ItemMeta head, @NotNull String identifier) {
        SkullMeta meta = (SkullMeta) head;
        if (isUsername(identifier)) return applySkin(head, Bukkit.getOfflinePlayer(identifier));
        if (identifier.contains("textures.minecraft.net")) return getValueFromTextures(meta, identifier);
        if (identifier.length() > 100 && isBase64(identifier)) return getSkullByValue(meta, identifier);
        return getTexturesFromUrlValue(meta, identifier);
    }

    @NotNull
    protected static SkullMeta getSkullByValue(@NotNull SkullMeta head, @NotNull String value) {
        Validate.notEmpty(value, "Skull value cannot be null or empty");
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", value));

        try {
            PROFILE_SETTER.invoke(head, profile);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return head;
    }

    @NotNull
    private static SkullMeta getValueFromTextures(@NotNull SkullMeta head, @NotNull String url) {
        return getSkullByValue(head, encodeBase64(VALUE_PROPERTY + url + "\"}}}"));
    }

    @NotNull
    private static SkullMeta getTexturesFromUrlValue(@NotNull SkullMeta head, @NotNull String urlValue) {
        return getValueFromTextures(head, TEXTURES + urlValue);
    }

    @NotNull
    private static String encodeBase64(@NotNull String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    /**
     * While RegEx is a little faster for small strings, this always checks strings with a length
     * greater than 100, so it'll perform a lot better.
     */
    private static boolean isBase64(@NotNull String base64) {
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
        //return BASE64.matcher(base64).matches();
    }

    @Nullable
    public static ItemBuilder.SkullTexture getSkinValue(@NotNull ItemMeta skull) {
        GameProfile profile = null;
        try {
            profile = Reflex.Companion.getProperty(skull, "profile", false, true, true);
        } catch (Exception ignored) {
        }
        if (profile != null && !profile.getProperties().get("textures").isEmpty()) {
            for (Property property : profile.getProperties().get("textures")) {
                if (!property.getValue().isEmpty()) {
                    return new ItemBuilder.SkullTexture(property.getValue(), profile.getId());
                }
            }
        }
        return null;
    }

    /**
     * https://help.minecraft.net/hc/en-us/articles/360034636712
     *
     * @param name the username to check.
     *
     * @return true if the string matches the Minecraft username rule, otherwise false.
     */
    private static boolean isUsername(@NotNull String name) {
        int len = name.length();
        if (len < 3 || len > 16) return false;

        // For some reasons Apache's Lists.charactersOf is faster than character indexing for small strings.
        for (char ch : Lists.charactersOf(name)) {
            if (ch != '_' && !(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z') && !(ch >= '0' && ch <= '9')) return false;
        }
        return true;
    }
}