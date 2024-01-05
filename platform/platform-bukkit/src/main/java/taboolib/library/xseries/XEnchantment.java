/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Crypto Morin
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

import com.google.common.base.Enums;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.Isolated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enchantment support with multiple aliases.
 * Uses EssentialsX enchantment list for aliases.
 * Enchantment levels do not start from 0, they start from 1
 * <p>
 * EssentialsX Enchantment: <a href="https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/enchantments/Enchantment.java">...</a>
 * Enchantment: <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html">...</a>
 * Enchanting: <a href="https://minecraft.wiki/w/Enchanting">...</a>
 *
 * @author Crypto Morin
 * @version 2.3.0
 * @see Enchantment
 */
@Isolated // Edited
public enum XEnchantment {
    ARROW_DAMAGE("POWER", "ARROW_DAMAGE", "ARROW_POWER", "AD"),
    ARROW_FIRE("FLAME", "FLAME_ARROW", "FIRE_ARROW", "AF"),
    ARROW_INFINITE("INFINITY", "INF_ARROWS", "INFINITE_ARROWS", "INFINITE", "UNLIMITED", "UNLIMITED_ARROWS", "AI"),
    ARROW_KNOCKBACK("PUNCH", "ARROW_KNOCKBACK", "ARROWKB", "ARROW_PUNCH", "AK"),
    BINDING_CURSE(true, "BINDING_CURSE", "BIND_CURSE", "BINDING", "BIND"),
    CHANNELING(true, "CHANNELLING", "CHANELLING", "CHANELING", "CHANNEL"),
    DAMAGE_ALL("SHARPNESS", "ALL_DAMAGE", "ALL_DMG", "SHARP", "DAL"),
    DAMAGE_ARTHROPODS("BANE_OF_ARTHROPODS", "ARDMG", "BANE_OF_ARTHROPOD", "ARTHROPOD", "DAR"),
    DAMAGE_UNDEAD("SMITE", "UNDEAD_DAMAGE", "DU"),
    DEPTH_STRIDER(true, "DEPTH", "STRIDER"),
    DIG_SPEED("EFFICIENCY", "MINE_SPEED", "CUT_SPEED", "DS", "EFF"),
    DURABILITY("UNBREAKING", "DURA"),
    FIRE_ASPECT(true, "FIRE", "MELEE_FIRE", "MELEE_FLAME", "FA"),
    FROST_WALKER(true, "FROST", "WALKER"),
    IMPALING(true, "IMPALE", "OCEAN_DAMAGE", "OCEAN_DMG"),
    KNOCKBACK(true, "K_BACK", "KB"),
    LOOT_BONUS_BLOCKS("FORTUNE", "BLOCKS_LOOT_BONUS", "FORT", "LBB"),
    LOOT_BONUS_MOBS("LOOTING", "MOB_LOOT", "MOBS_LOOT_BONUS", "LBM"),
    LOYALTY(true, "LOYAL", "RETURN"),
    LUCK("LUCK_OF_THE_SEA", "LUCK_OF_SEA", "LUCK_OF_SEAS", "ROD_LUCK"),
    LURE(true, "ROD_LURE"),
    MENDING(true),
    MULTISHOT(true, "TRIPLE_SHOT"),
    OXYGEN("RESPIRATION", "BREATH", "BREATHING", "O2", "O"),
    PIERCING(true),
    PROTECTION_ENVIRONMENTAL("PROTECTION", "PROTECT", "PROT"),
    PROTECTION_EXPLOSIONS("BLAST_PROTECTION", "BLAST_PROTECT", "EXPLOSIONS_PROTECTION", "EXPLOSION_PROTECTION", "BLAST_PROTECTION", "PE"),
    PROTECTION_FALL("FEATHER_FALLING", "FALL_PROT", "FEATHER_FALL", "FALL_PROTECTION", "FEATHER_FALLING", "PFA"),
    PROTECTION_FIRE("FIRE_PROTECTION", "FIRE_PROT", "FIRE_PROTECT", "FIRE_PROTECTION", "FLAME_PROTECTION", "FLAME_PROTECT", "FLAME_PROT", "PF"),
    PROTECTION_PROJECTILE("PROJECTILE_PROTECTION", "PROJECTILE_PROTECTION", "PROJ_PROT", "PP"),
    QUICK_CHARGE(true, "QUICKCHARGE", "QUICK_DRAW", "FAST_CHARGE", "FAST_DRAW"),
    RIPTIDE(true, "RIP", "TIDE", "LAUNCH"),
    SILK_TOUCH(true, "SOFT_TOUCH", "ST"),
    SOUL_SPEED(true, "SPEED_SOUL", "SOUL_RUNNER"),
    SWEEPING_EDGE("SWEEPING", "SWEEPING_EDGE", "SWEEP_EDGE"),
    SWIFT_SNEAK(true, "SNEAK_SWIFT"),
    THORNS(true, "HIGHCRIT", "THORN", "HIGHERCRIT", "T"),
    VANISHING_CURSE(true, "VANISHING_CURSE", "VANISH_CURSE", "VANISHING", "VANISH"),
    WATER_WORKER("AQUA_AFFINITY", "WATER_WORKER", "AQUA_AFFINITY", "WATER_MINE", "WW");

    /**
     * Cached list of {@link XEnchantment#values()} to avoid allocating memory for
     *
     * @since 1.0.0
     */
    public static final XEnchantment[] VALUES = values();

    /**
     * Entity types that {@link #DAMAGE_UNDEAD} enchantment is effective against.
     * This set is unmodifiable.
     *
     * @since 1.2.0
     */
    public static final Set<EntityType> EFFECTIVE_SMITE_ENTITIES;
    /**
     * Entity types that {@link #DAMAGE_ARTHROPODS} enchantment is effective against.
     * This set is unmodifiable.
     *
     * @since 1.2.0
     */
    public static final Set<EntityType> EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES;

    static {
        EntityType bee = Enums.getIfPresent(EntityType.class, "BEE").orNull();
        EntityType phantom = Enums.getIfPresent(EntityType.class, "PHANTOM").orNull();
        EntityType drowned = Enums.getIfPresent(EntityType.class, "DROWNED").orNull();
        EntityType witherSkeleton = Enums.getIfPresent(EntityType.class, "WITHER_SKELETON").orNull();
        EntityType skeletonHorse = Enums.getIfPresent(EntityType.class, "SKELETON_HORSE").orNull();
        EntityType stray = Enums.getIfPresent(EntityType.class, "STRAY").orNull();
        EntityType husk = Enums.getIfPresent(EntityType.class, "HUSK").orNull();

        Set<EntityType> arthorposEffective = EnumSet.of(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE);
        if (bee != null) arthorposEffective.add(bee);
        EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES = Collections.unmodifiableSet(arthorposEffective);

        Set<EntityType> smiteEffective = EnumSet.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER);
        if (phantom != null) smiteEffective.add(phantom);
        if (drowned != null) smiteEffective.add(drowned);
        if (witherSkeleton != null) smiteEffective.add(witherSkeleton);
        if (skeletonHorse != null) smiteEffective.add(skeletonHorse);
        if (stray != null) smiteEffective.add(stray);
        if (husk != null) smiteEffective.add(husk);
        EFFECTIVE_SMITE_ENTITIES = Collections.unmodifiableSet(smiteEffective);
    }

    @Nullable
    private final Enchantment enchantment;

    XEnchantment(@NotNull String... names) {
        this(false, names);
    }

    /**
     * If an enchantment has {@code self} as true, it means that
     * the vanilla enchantment name matches the Bukkit name.
     *
     * @see NamespacedKey#getKey()
     */
    @SuppressWarnings("deprecation")
    XEnchantment(boolean self, @NotNull String... aliases) {
        Data.NAMES.put(this.name(), this);
        for (String legacy : aliases) Data.NAMES.put(legacy, this);

        Enchantment enchantment;
        if (Data.ISFLAT) {
            String vanilla = self ? this.name() : aliases[0];
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(vanilla.toLowerCase(Locale.ENGLISH)));
        } else enchantment = Enchantment.getByName(this.name());
        this.enchantment = enchantment;
    }

    /**
     * Checks if {@link #DAMAGE_UNDEAD Smite} is effective
     * against this type of mob.
     *
     * @param type the type of the mob.
     * @return true if smite enchantment is effective against the mob, otherwise false.
     * @since 1.1.0
     */
    public static boolean isSmiteEffectiveAgainst(@Nullable EntityType type) {
        return type != null && EFFECTIVE_SMITE_ENTITIES.contains(type);
    }

    /**
     * Checks if {@link #DAMAGE_ARTHROPODS Bane of Arthropods} is effective
     * against this type of mob.
     *
     * @param type the type of the mob.
     * @return true if Bane of Arthropods enchantment is effective against the mob, otherwise false.
     * @since 1.1.0
     */
    public static boolean isArthropodsEffectiveAgainst(@Nullable EntityType type) {
        return type != null && EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES.contains(type);
    }

    /**
     * Attempts to build the string like an enum name.<br>
     * Removes all the spaces, numbers and extra non-English characters. Also removes some config/in-game based strings.
     * While this method is hard to maintain, it's extremely efficient. It's approximately more than x5 times faster than
     * the normal RegEx + String Methods approach for both formatted and unformatted material names.
     *
     * @param name the enchantment name to format.
     * @return an enum name.
     * @since 1.0.0
     */
    @NotNull
    private static String format(@NotNull String name) {
        int len = name.length();
        char[] chs = new char[len];
        int count = 0;
        boolean appendUnderline = false;

        for (int i = 0; i < len; i++) {
            char ch = name.charAt(i);

            if (!appendUnderline && count != 0 && (ch == '-' || ch == ' ' || ch == '_') && chs[count] != '_')
                appendUnderline = true;
            else {
                if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                    if (appendUnderline) {
                        chs[count++] = '_';
                        appendUnderline = false;
                    }
                    chs[count++] = (char) (ch & 0x5f);
                }
            }
        }

        return new String(chs, 0, count);
    }

    /**
     * Gets an enchantment from Vanilla and bukkit names.
     * There are also some aliases available.
     *
     * @param enchantment the name of the enchantment.
     * @return an enchantment.
     * @since 1.0.0
     */
    @NotNull
    public static Optional<XEnchantment> matchXEnchantment(@NotNull String enchantment) {
        if (enchantment == null || enchantment.isEmpty())
            throw new IllegalArgumentException("Enchantment name cannot be null or empty");
        return Optional.ofNullable(Data.NAMES.get(format(enchantment)));
    }

    /**
     * Gets an enchantment from Vanilla and bukkit names.
     * There are also some aliases available.
     *
     * @param enchantment the enchantment.
     * @return an enchantment.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 1.0.0
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public static XEnchantment matchXEnchantment(@NotNull Enchantment enchantment) {
        Objects.requireNonNull(enchantment, "Cannot parse XEnchantment of a null enchantment");
        return Objects.requireNonNull(Data.NAMES.get(enchantment.getName()), () -> "Unsupported enchantment: " + enchantment.getName());
    }

    /**
     * Gets the enchanted book of this enchantment.
     *
     * @param level the level of this enchantment.
     * @return an enchanted book.
     * @since 1.0.0
     */
    @NotNull
    public ItemStack getBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();

        meta.addStoredEnchant(this.enchantment, level, true);
        book.setItemMeta(meta);
        return book;
    }

    /**
     * Parse the Vanilla enchantment.
     *
     * @return a Vanilla  enchantment.
     * @since 1.0.0
     */
    @Nullable
    public Enchantment getEnchant() {
        return this.enchantment;
    }

    /**
     * Checks if this enchantment is supported and registered in the current Minecraft version.
     * <p>
     * An invocation of this method yields exactly the same result as the expression:
     * <p>
     * <blockquote>
     * {@link #getEnchant()} != null
     * </blockquote>
     *
     * @return true if the current version has this enchantment, otherwise false.
     * @since 1.0.0
     */
    public boolean isSupported() {
        return enchantment != null;
    }

    /**
     * Checks if this enchantment is supported in the current version and
     * returns itself if yes.
     * <p>
     * In the other case, the alternate enchantment will get returned,
     * no matter if it is supported or not.
     *
     * @param alternateEnchantment the enchantment to get if this one is not supported.
     * @return this enchantment or the {@code alternateEnchantment} if not supported.
     */
    @Nullable
    public XEnchantment or(@Nullable XEnchantment alternateEnchantment) {
        return isSupported() ? this : alternateEnchantment;
    }

    /**
     * In most cases you should be using {@link #name()} instead.
     *
     * @return a friendly readable string name.
     */
    @Override
    @NotNull
    public String toString() {
        return Arrays.stream(name().split("_"))
                .map(t -> t.charAt(0) + t.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Used for data that need to be accessed during enum initialization.
     *
     * @since 2.0.0
     */
    private static final class Data {
        private static final boolean ISFLAT;
        private static final Map<String, XEnchantment> NAMES = new HashMap<>();

        static {
            boolean flat;
            try {
                Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
                Class<?> enchantmentClass = Class.forName("org.bukkit.enchantments.Enchantment");
                enchantmentClass.getDeclaredMethod("getByKey", namespacedKeyClass);
                flat = true;
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                flat = false;
            }
            ISFLAT = flat;
        }
    }
}