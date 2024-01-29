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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.Reflex;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * <b>SkullUtils</b> - Apply skull texture from different sources.<br>
 * Skull Meta: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/SkullMeta.html
 * Mojang API: https://wiki.vg/Mojang_API
 * <p>
 * Some websites to get custom heads:
 * <ul>
 *     <li>https://minecraft-heads.com/</li>
 * </ul>
 * <p>
 * The basic premise behind this API is that the final skull data is contained in a {@link GameProfile}
 * either by ID, name or encoded textures URL property.
 *
 * @author Crypto Morin
 * @version 4.0.3
 * @see XMaterial
 */
public class XSkull {

    protected static final MethodHandle
            CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
            CRAFT_META_SKULL_BLOCK_SETTER, PROPERTY_GET_VALUE;

    /**
     * Some people use this without quotes surrounding the keys, not sure what that'd work.
     */
    private static final String VALUE_PROPERTY = "{\"textures\":{\"SKIN\":{\"url\":\"";
    private static final boolean SUPPORTS_UUID = ReflectionUtils.supports(12);

    /**
     * We'll just return an x shaped hardcoded skull.
     * https://minecraft-heads.com/custom-heads/miscellaneous/58141-cross
     */
    private static final String INVALID_BASE64 =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0=";

    /**
     * They don't seem to use anything complicated, but the length is inconsistent for some reasons.
     * It doesn't seem like uppercase characters are used either.
     */
    private static final Pattern MOJANG_SHA256_APPROX = Pattern.compile("[0-9a-z]{60,70}");

    /**
     * In v1.20.2 there were some changes to the mojang API.
     */
    private static final boolean NULLABILITY_RECORD_UPDATE = !ReflectionUtils.VERSION.equals("v1_20_R1") && ReflectionUtils.supports(20);

    /**
     * Does using a random UUID have any advantage?
     */
    private static final UUID GAME_PROFILE_EMPTY_UUID = NULLABILITY_RECORD_UPDATE ? new UUID(0, 0) : null;
    private static final String GAME_PROFILE_EMPTY_NAME = NULLABILITY_RECORD_UPDATE ? "" : null;

    /**
     * The value after this URL is probably an SHA-252 value that Mojang uses to unique identify player skins.
     * <br>
     * This <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin/Cape">wiki</a> documents how to
     * get base64 information from player's UUID.
     */
    private static final String TEXTURES = "https://textures.minecraft.net/texture/";
    private static final Class<?> CLASS_PROPERTY;

    static {
        try {
            CLASS_PROPERTY = Class.forName("com.mojang.authlib.properties.Property");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle profileSetter = null, profileGetter = null, blockSetter = null, propGetval = null, propConstructor = null;

        try {
            Class<?> CraftMetaSkull = ReflectionUtils.getCraftClass("inventory.CraftMetaSkull");
            Field profile = CraftMetaSkull.getDeclaredField("profile");
            profile.setAccessible(true);
            profileGetter = lookup.unreflectGetter(profile);

            try {
                // https://github.com/CryptoMorin/XSeries/issues/169
                Method setProfile = CraftMetaSkull.getDeclaredMethod("setProfile", GameProfile.class);
                setProfile.setAccessible(true);
                profileSetter = lookup.unreflect(setProfile);
            } catch (NoSuchMethodException e) {
                profileSetter = lookup.unreflectSetter(profile);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            // CraftSkull private GameProfile profile;
            Class<?> CraftSkullBlock = ReflectionUtils.getCraftClass("block.CraftSkull");
            Field field = CraftSkullBlock.getDeclaredField("profile");
            field.setAccessible(true);
            blockSetter = lookup.unreflectSetter(field);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (NULLABILITY_RECORD_UPDATE) {
            try {
                propGetval = lookup.findVirtual(CLASS_PROPERTY, "value", MethodType.methodType(String.class));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                propGetval = lookup.findVirtual(CLASS_PROPERTY, "getValue", MethodType.methodType(String.class));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        PROPERTY_GET_VALUE = propGetval;
        CRAFT_META_SKULL_PROFILE_SETTER = profileSetter;
        CRAFT_META_SKULL_PROFILE_GETTER = profileGetter;
        CRAFT_META_SKULL_BLOCK_SETTER = blockSetter;
    }

    @SuppressWarnings("deprecation")
    @NotNull
    public static ItemStack getSkull(@NotNull UUID id) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (SUPPORTS_UUID) meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        else meta.setOwner(Bukkit.getOfflinePlayer(id).getName());

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
        // @formatter:off
        switch (detectSkullValueType(identifier)) {
            case UUID: return applySkin(head, Bukkit.getOfflinePlayer(UUID.fromString(identifier)));
            case NAME: return applySkin(head, Bukkit.getOfflinePlayer(identifier));
            case BASE64:       return setSkullBase64(meta, identifier);
            case TEXTURE_URL:  return setSkullBase64(meta, encodeTexturesURL(identifier));
            case TEXTURE_HASH: return setSkullBase64(meta, encodeTexturesURL(TEXTURES + identifier));
            case UNKNOWN:      return setSkullBase64(meta, INVALID_BASE64);
            default: throw new AssertionError("Unknown skull value");
        }
        // @formatter:on
    }

    @NotNull
    protected static SkullMeta setSkullBase64(@NotNull SkullMeta head, @NotNull String value) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("Skull value cannot be null or empty");
        GameProfile profile = profileFromBase64(value);

        try {
            CRAFT_META_SKULL_PROFILE_SETTER.invoke(head, profile);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return head;
    }

    @NotNull
    public static GameProfile profileFromBase64(String value) {
        // Use an empty string instead of null for the name parameter because it's now null-checked since 1.20.2.
        // It doesn't seem to affect functionality.
        GameProfile profile = new GameProfile(GAME_PROFILE_EMPTY_UUID, GAME_PROFILE_EMPTY_NAME);
        profile.getProperties().put("textures", new Property("textures", value));
        return profile;
    }

    @NotNull
    public static GameProfile profileFromPlayer(OfflinePlayer player) {
        return new GameProfile(player.getUniqueId(), player.getName());
    }

    @NotNull
    public static GameProfile detectProfileFromString(String identifier) {
        // @formatter:off sometimes programming is just art that a machine can't understand :)
        switch (detectSkullValueType(identifier)) {
            case UUID:         return new GameProfile(UUID.fromString(               identifier), GAME_PROFILE_EMPTY_NAME);
            case NAME:         return new GameProfile(GAME_PROFILE_EMPTY_UUID,                          identifier);
            case BASE64:       return profileFromBase64(                             identifier);
            case TEXTURE_URL:  return profileFromBase64(encodeTexturesURL(           identifier));
            case TEXTURE_HASH: return profileFromBase64(encodeTexturesURL(TEXTURES + identifier));
            case UNKNOWN:      return profileFromBase64(INVALID_BASE64); // This can't be cached because the caller might change it.
            default: throw new AssertionError("Unknown skull value");
        }
        // @formatter:on
    }

    public static ValueType detectSkullValueType(String identifier) {
        try {
            UUID.fromString(identifier);
            return ValueType.UUID;
        } catch (IllegalArgumentException ignored) {
        }

        if (isUsername(identifier)) return ValueType.NAME;
        if (identifier.contains("textures.minecraft.net")) return ValueType.TEXTURE_URL;
        if (identifier.length() > 100 && isBase64(identifier)) return ValueType.BASE64;

        // We'll just "assume" that it's a textures.minecraft.net hash without the URL part.
        if (MOJANG_SHA256_APPROX.matcher(identifier).matches()) return ValueType.TEXTURE_HASH;

        return ValueType.UNKNOWN;
    }

    public static void setSkin(@NotNull Block block, @NotNull String value) {
        Objects.requireNonNull(block, "Can't set skin of null block");

        BlockState state = block.getState();
        if (!(state instanceof Skull)) return;
        Skull skull = (Skull) state;

        GameProfile profile = detectProfileFromString(value);
        try {
            CRAFT_META_SKULL_BLOCK_SETTER.invoke(skull, profile);
        } catch (Throwable e) {
            throw new RuntimeException("Error while setting block skin with value: " + value, e);
        }

        skull.update(true);
    }

    public static String encodeTexturesURL(String url) {
        // String.format bad!
        return encodeBase64(VALUE_PROPERTY + url + "\"}}}");
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
    public static SkullTexture getSkinValue(@NotNull ItemMeta skull) {
        GameProfile profile = null;
        try {
            profile = Reflex.Companion.getProperty(skull, "profile", false, true, true);
        } catch (Exception ignored) {
        }
        if (profile != null && !profile.getProperties().get("textures").isEmpty()) {
            for (Object property : profile.getProperties().get("textures")) {
                String value = getPropertyValue(property);
                if (!value.isEmpty()) {
                    return new SkullTexture(value, profile.getId());
                }
            }
        }
        return null;
    }

    /**
     * They changed Property to a Java record in 1.20.2
     *
     * @since 4.0.1
     */
    private static String getPropertyValue(Object property) {
        try {
            return (String) PROPERTY_GET_VALUE.invoke(property);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * https://help.minecraft.net/hc/en-us/articles/360034636712
     *
     * @param name the username to check.
     * @return true if the string matches the Minecraft username rule, otherwise false.
     */
    private static boolean isUsername(@NotNull String name) {
        int len = name.length();
        if (len > 16) return false; // Yes, in the old Minecraft 1 letter usernames were a thing.

        // For some reasons Apache's Lists.charactersOf is faster than character indexing for small strings.
        for (char ch : Lists.charactersOf(name)) {
            if (ch != '_' && !(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z') && !(ch >= '0' && ch <= '9'))
                return false;
        }
        return true;
    }

    public enum ValueType {NAME, UUID, BASE64, TEXTURE_URL, TEXTURE_HASH, UNKNOWN}

    public static class SkullTexture {

        private final String textures;
        private final UUID uuid;

        public SkullTexture(String textures, UUID uuid) {
            this.textures = textures;
            this.uuid = uuid;
        }

        public String getTexture() {
            return textures;
        }

        public UUID getUuid() {
            return uuid;
        }
    }
}