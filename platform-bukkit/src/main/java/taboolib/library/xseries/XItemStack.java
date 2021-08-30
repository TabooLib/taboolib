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

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import taboolib.common.Isolated;
import taboolib.library.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static taboolib.library.xseries.XMaterial.supports;

/**
 * <b>XItemStack</b> - YAML Item Serializer<br>
 * Using ConfigurationSection Example:
 * <pre>
 *     ConfigurationSection section = plugin.getConfig().getConfigurationSection("staffs.dragon-staff");
 *     ItemStack item = XItemStack.deserialize(section);
 * </pre>
 * ItemStack: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html
 *
 * @author Crypto Morin
 * @version 5.2.0.1.1
 * @see XMaterial
 * @see ItemStack
 */
@SuppressWarnings("ConstantConditions")
@Isolated
public final class XItemStack {

    private XItemStack() {
    }

    /**
     * Writes an ItemStack object into a config.
     * The config file will not save after the object is written.
     *
     * @param item   the ItemStack to serialize.
     * @param config the config section to write this item to.
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
    public static void serialize(@Nonnull ItemStack item, @Nonnull ConfigurationSection config) {
        Objects.requireNonNull(item, "Cannot serialize a null item");
        Objects.requireNonNull(config, "Cannot serialize item from a null configuration section.");
        ItemMeta meta = item.getItemMeta();
        // Material
        config.set("material", item.getType().name());
        // Amount
        if (item.getAmount() > 1) {
            config.set("amount", item.getAmount());
        }
        if (meta == null) {
            return;
        }
        // Durability - Damage
        if (supports(13)) {
            if (meta instanceof Damageable) {
                Damageable damageable = (Damageable) meta;
                if (damageable.hasDamage()) {
                    config.set("damage", damageable.getDamage());
                }
            }
        } else {
            config.set("damage", item.getDurability());
        }
        // Display Name & Lore
        if (meta.hasDisplayName()) {
            config.set("name", untranslate(meta.getDisplayName()));
        }
        if (meta.hasLore()) {
            config.set("lore", untranslate(meta.getLore()));
        }
        if (supports(14)) {
            if (meta.hasCustomModelData()) {
                config.set("custom-model", meta.getCustomModelData());
            }
        }
        if (supports(11)) {
            if (meta.isUnbreakable()) {
                config.set("unbreakable", true);
            }
        }
        // Enchantments
        for (Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
            String entry = "enchants." + XEnchantment.matchXEnchantment(enchant.getKey()).name();
            config.set(entry, enchant.getValue());
        }
        // Flags
        if (!meta.getItemFlags().isEmpty()) {
            Set<ItemFlag> flags = meta.getItemFlags();
            List<String> flagNames = new ArrayList<>(flags.size());
            for (ItemFlag flag : flags) {
                flagNames.add(flag.name());
            }
            config.set("flags", flagNames);
        }
        if (supports(13)) {
            Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();
            if (attributes != null) {
                for (Map.Entry<Attribute, AttributeModifier> attribute : attributes.entries()) {
                    String path = "attributes." + attribute.getKey().name() + '.';
                    AttributeModifier modifier = attribute.getValue();
                    config.set(path + "id", modifier.getUniqueId().toString());
                    config.set(path + "name", modifier.getName());
                    config.set(path + "amount", modifier.getAmount());
                    config.set(path + "operation", modifier.getOperation().name());
                    config.set(path + "slot", modifier.getSlot());
                }
            }
        }
        if (meta instanceof BlockStateMeta) {
            BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
            BlockState state = Objects.requireNonNull(bsm).getBlockState();
            if (supports(11) && state instanceof ShulkerBox) {
                ShulkerBox box = (ShulkerBox) state;
                ConfigurationSection shulker = config.createSection("shulker");
                int i = 0;
                for (ItemStack itemInBox : box.getInventory().getContents()) {
                    serialize(itemInBox, shulker.createSection(Integer.toString(i)));
                    i++;
                }
            } else if (state instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) state;
                config.set("spawner", cs.getSpawnedType().name());
            }
        } else if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta book = (EnchantmentStorageMeta) meta;
            for (Map.Entry<Enchantment, Integer> enchant : book.getStoredEnchants().entrySet()) {
                String entry = "stored-enchants." + XEnchantment.matchXEnchantment(enchant.getKey()).name();
                config.set(entry, enchant.getValue());
            }
        } else if (meta instanceof SkullMeta) {
            config.set("skull", XSkull.getSkinValue(meta));
        } else if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            ConfigurationSection patterns = config.createSection("patterns");
            for (Pattern pattern : banner.getPatterns()) {
                patterns.set(pattern.getPattern().name(), pattern.getColor().name());
            }
        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            Color color = leather.getColor();
            config.set("color", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
        } else if (meta instanceof PotionMeta) {
            if (supports(9)) {
                PotionMeta potion = (PotionMeta) meta;
                List<PotionEffect> customEffects = potion.getCustomEffects();
                List<String> effects = new ArrayList<>(customEffects.size());
                for (PotionEffect effect : customEffects) {
                    effects.add(effect.getType().getName() + ", " + effect.getDuration() + ", " + effect.getAmplifier());
                }
                config.set("custom-effects", effects);
                PotionData potionData = potion.getBasePotionData();
                config.set("base-effect", potionData.getType().name() + ", " + potionData.isExtended() + ", " + potionData.isUpgraded());
                if (potion.hasColor()) {
                    config.set("color", potion.getColor().asRGB());
                }
            } else {
                //check for water bottles in 1.8
                if (item.getDurability() != 0) {
                    Potion potion = Potion.fromItemStack(item);
                    config.set("level", potion.getLevel());
                    config.set("base-effect", potion.getType().name() + ", " + potion.hasExtendedDuration() + ", " + potion.isSplash());
                }
            }
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            config.set("power", firework.getPower());
            int i = 0;
            for (FireworkEffect fw : firework.getEffects()) {
                config.set("firework." + i + ".type", fw.getType().name());
                ConfigurationSection fwc = config.getConfigurationSection("firework." + i);
                fwc.set("flicker", fw.hasFlicker());
                fwc.set("trail", fw.hasTrail());
                List<Color> fwBaseColors = fw.getColors();
                List<Color> fwFadeColors = fw.getFadeColors();
                List<String> baseColors = new ArrayList<>(fwBaseColors.size());
                List<String> fadeColors = new ArrayList<>(fwFadeColors.size());
                ConfigurationSection colors = fwc.createSection("colors");
                for (Color color : fwBaseColors) {
                    baseColors.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                }
                colors.set("base", baseColors);
                for (Color color : fwFadeColors) {
                    fadeColors.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                }
                colors.set("fade", fadeColors);
                i++;
            }
        } else if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            ConfigurationSection bookInfo = config.createSection("book");
            bookInfo.set("title", book.getTitle());
            bookInfo.set("author", book.getAuthor());
            if (supports(9)) {
                BookMeta.Generation generation = book.getGeneration();
                if (generation != null) {
                    bookInfo.set("generation", book.getGeneration().toString());
                }
            }
            bookInfo.set("pages", book.getPages());
        } else if (meta instanceof MapMeta) {
            MapMeta map = (MapMeta) meta;
            ConfigurationSection mapSection = config.createSection("map");
            mapSection.set("scaling", map.isScaling());
            if (supports(11)) {
                if (map.hasLocationName()) {
                    mapSection.set("location", map.getLocationName());
                }
                if (map.hasColor()) {
                    Color color = map.getColor();
                    mapSection.set("color", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                }
            }
            if (supports(14)) {
                if (map.hasMapView()) {
                    MapView mapView = map.getMapView();
                    ConfigurationSection view = mapSection.createSection("view");
                    view.set("scale", mapView.getScale().toString());
                    view.set("world", mapView.getWorld().getName());
                    ConfigurationSection centerSection = view.createSection("center");
                    centerSection.set("x", mapView.getCenterX());
                    centerSection.set("z", mapView.getCenterZ());
                    view.set("locked", mapView.isLocked());
                    view.set("tracking-position", mapView.isTrackingPosition());
                    view.set("unlimited-tracking", mapView.isUnlimitedTracking());
                }
            }
        } else if (supports(17)) {
            if (meta instanceof AxolotlBucketMeta) {
                AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                if (bucket.hasVariant()) {
                    config.set("variant", bucket.getVariant().toString());
                }
            }
        } else if (supports(16)) {
            if (meta instanceof CompassMeta) {
                CompassMeta compass = (CompassMeta) meta;
                ConfigurationSection subSection = config.createSection("lodestone");
                subSection.set("tracked", compass.isLodestoneTracked());
                if (compass.hasLodestone()) {
                    Location location = compass.getLodestone();
                    subSection.set("location.world", location.getWorld().getName());
                    subSection.set("location.x", location.getX());
                    subSection.set("location.y", location.getY());
                    subSection.set("location.z", location.getZ());
                }
            }
        } else if (supports(14)) {
            if (meta instanceof CrossbowMeta) {
                CrossbowMeta crossbow = (CrossbowMeta) meta;
                int i = 0;
                for (ItemStack projectiles : crossbow.getChargedProjectiles()) {
                    serialize(projectiles, config.getConfigurationSection("projectiles." + i));
                    i++;
                }
            } else if (meta instanceof TropicalFishBucketMeta) {
                TropicalFishBucketMeta tropical = (TropicalFishBucketMeta) meta;
                config.set("pattern", tropical.getPattern().name());
                config.set("color", tropical.getBodyColor().name());
                config.set("pattern-color", tropical.getPatternColor().name());
            } else if (meta instanceof SuspiciousStewMeta) {
                SuspiciousStewMeta stew = (SuspiciousStewMeta) meta;
                List<PotionEffect> customEffects = stew.getCustomEffects();
                List<String> effects = new ArrayList<>(customEffects.size());
                for (PotionEffect effect : customEffects) {
                    effects.add(effect.getType().getName() + ", " + effect.getDuration() + ", " + effect.getAmplifier());
                }
                config.set("effects", effects);
            }
        } else if (!supports(13)) {
            // Spawn Eggs
            if (supports(11)) {
                if (meta instanceof SpawnEggMeta) {
                    SpawnEggMeta spawnEgg = (SpawnEggMeta) meta;
                    config.set("creature", spawnEgg.getSpawnedType().getName());
                }
            } else {
                MaterialData data = item.getData();
                if (data instanceof SpawnEgg) {
                    SpawnEgg spawnEgg = (SpawnEgg) data;
                    config.set("creature", spawnEgg.getSpawnedType().getName());
                }
            }
        }
    }

    /**
     * Deserialize an ItemStack from the config.
     *
     * @param config the config section to deserialize the ItemStack object from.
     * @return a deserialized ItemStack.
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
    @Nullable
    public static ItemStack deserialize(@Nonnull ConfigurationSection config) {
        Objects.requireNonNull(config, "Cannot deserialize item to a null configuration section.");
        // Material
        String material = config.getString("material");
        if (material == null) {
            return null;
        }
        Optional<XMaterial> matOpt = XMaterial.matchXMaterial(material);
        if (!matOpt.isPresent()) {
            return null;
        }
        // Build
        ItemStack item = matOpt.get().parseItem();
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        // Amount
        int amount = config.getInt("amount");
        if (amount > 1) {
            item.setAmount(amount);
        }
        // Durability - Damage
        if (supports(13)) {
            if (meta instanceof Damageable) {
                int damage = config.getInt("damage");
                if (damage > 0) {
                    ((Damageable) meta).setDamage(damage);
                }
            }
        } else {
            int damage = config.getInt("damage");
            if (damage > 0) {
                item.setDurability((short) damage);
            }
        }
        // Special Items
        if (meta instanceof SkullMeta) {
            String skull = config.getString("skull");
            if (skull != null) {
                XSkull.applySkin(meta, skull);
            }
        } else if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            ConfigurationSection patterns = config.getConfigurationSection("patterns");
            if (patterns != null) {
                for (String pattern : patterns.getKeys(false)) {
                    PatternType type = PatternType.getByIdentifier(pattern);
                    if (type == null) {
                        type = Enums.getIfPresent(PatternType.class, pattern.toUpperCase(Locale.ENGLISH)).or(PatternType.BASE);
                    }
                    DyeColor color = Enums.getIfPresent(DyeColor.class, patterns.getString(pattern).toUpperCase(Locale.ENGLISH)).or(DyeColor.WHITE);
                    banner.addPattern(new Pattern(color, type));
                }
            }
        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            String colorStr = config.getString("color");
            if (colorStr != null) {
                leather.setColor(parseColor(colorStr));
            }
        } else if (meta instanceof PotionMeta) {
            if (supports(9)) {
                PotionMeta potion = (PotionMeta) meta;
                for (String effects : config.getStringList("custom-effects")) {
                    PotionEffect effect = XPotion.parsePotionEffectFromString(effects);
                    potion.addCustomEffect(effect, true);
                }
                String baseEffect = config.getString("base-effect");
                if (!Strings.isNullOrEmpty(baseEffect)) {
                    String[] split = StringUtils.split(baseEffect, ',');
                    PotionType type = Enums.getIfPresent(PotionType.class, split[0].trim().toUpperCase(Locale.ENGLISH)).or(PotionType.UNCRAFTABLE);
                    boolean extended = split.length != 1 && Boolean.parseBoolean(split[1].trim());
                    boolean upgraded = split.length > 2 && Boolean.parseBoolean(split[2].trim());
                    PotionData potionData = new PotionData(type, extended, upgraded);
                    potion.setBasePotionData(potionData);
                }
                if (config.contains("color")) {
                    potion.setColor(Color.fromRGB(config.getInt("color")));
                }
            } else {
                if (config.contains("level")) {
                    int level = config.getInt("level");
                    String baseEffect = config.getString("base-effect");
                    if (!Strings.isNullOrEmpty(baseEffect)) {
                        String[] split = StringUtils.split(baseEffect, ',');
                        PotionType type = Enums.getIfPresent(PotionType.class, split[0].trim().toUpperCase(Locale.ENGLISH)).or(PotionType.SLOWNESS);
                        boolean extended = split.length != 1 && Boolean.parseBoolean(split[1].trim());
                        boolean splash = split.length > 2 && Boolean.parseBoolean(split[2].trim());
                        item = (new Potion(type, level, splash, extended)).toItemStack(1);
                    }
                }
            }
        } else if (meta instanceof BlockStateMeta) {
            BlockStateMeta bsm = (BlockStateMeta) meta;
            BlockState state = bsm.getBlockState();
            if (state instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner) state;
                spawner.setSpawnedType(Enums.getIfPresent(EntityType.class, config.getString("spawner").toUpperCase(Locale.ENGLISH)).orNull());
                spawner.update(true);
                bsm.setBlockState(spawner);
            } else if (supports(11) && state instanceof ShulkerBox) {
                ConfigurationSection shulkerSection = config.getConfigurationSection("shulker");
                if (shulkerSection != null) {
                    ShulkerBox box = (ShulkerBox) state;
                    for (String key : shulkerSection.getKeys(false)) {
                        ItemStack boxItem = deserialize(shulkerSection.getConfigurationSection(key));
                        int slot = NumberUtils.toInt(key, 0);
                        box.getInventory().setItem(slot, boxItem);
                    }
                    box.update(true);
                    bsm.setBlockState(box);
                }
            } else if (state instanceof Banner) {
                Banner banner = (Banner) state;
                ConfigurationSection patterns = config.getConfigurationSection("patterns");
                if (!supports(14)) {
                    banner.setBaseColor(DyeColor.WHITE);
                }
                if (patterns != null) {
                    for (String pattern : patterns.getKeys(false)) {
                        PatternType type = PatternType.getByIdentifier(pattern);
                        if (type == null) {
                            type = Enums.getIfPresent(PatternType.class, pattern.toUpperCase(Locale.ENGLISH)).or(PatternType.BASE);
                        }
                        DyeColor color = Enums.getIfPresent(DyeColor.class, patterns.getString(pattern).toUpperCase(Locale.ENGLISH)).or(DyeColor.WHITE);
                        banner.addPattern(new Pattern(color, type));
                    }
                    banner.update(true);
                    bsm.setBlockState(banner);
                }
            }
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            firework.setPower(config.getInt("power"));
            ConfigurationSection fireworkSection = config.getConfigurationSection("firework");
            if (fireworkSection != null) {
                FireworkEffect.Builder builder = FireworkEffect.builder();
                for (String fws : fireworkSection.getKeys(false)) {
                    ConfigurationSection fw = config.getConfigurationSection("firework." + fws);
                    builder.flicker(fw.getBoolean("flicker"));
                    builder.trail(fw.getBoolean("trail"));
                    builder.with(Enums.getIfPresent(FireworkEffect.Type.class, fw.getString("type").toUpperCase(Locale.ENGLISH)).or(FireworkEffect.Type.STAR));
                    ConfigurationSection colorsSection = fw.getConfigurationSection("colors");
                    List<String> fwColors = colorsSection.getStringList("base");
                    List<Color> colors = new ArrayList<>(fwColors.size());
                    for (String colorStr : fwColors) {
                        colors.add(parseColor(colorStr));
                    }
                    builder.withColor(colors);
                    fwColors = colorsSection.getStringList("fade");
                    colors = new ArrayList<>(fwColors.size());
                    for (String colorStr : fwColors) {
                        colors.add(parseColor(colorStr));
                    }
                    builder.withFade(colors);
                    firework.addEffect(builder.build());
                }
            }
        } else if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            ConfigurationSection bookInfo = config.getConfigurationSection("book");
            if (bookInfo != null) {
                book.setTitle(bookInfo.getString("title"));
                book.setAuthor(bookInfo.getString("author"));
                book.setPages(bookInfo.getStringList("pages"));
                if (supports(9)) {
                    String generationValue = bookInfo.getString("generation");
                    if (generationValue != null) {
                        BookMeta.Generation generation = Enums.getIfPresent(BookMeta.Generation.class, generationValue).orNull();
                        book.setGeneration(generation);
                    }
                }
            }
        } else if (meta instanceof MapMeta) {
            MapMeta map = (MapMeta) meta;
            ConfigurationSection mapSection = config.getConfigurationSection("map");
            if (mapSection != null) {
                map.setScaling(mapSection.getBoolean("scaling"));
                if (supports(11)) {
                    if (mapSection.isSet("location")) {
                        map.setLocationName(mapSection.getString("location"));
                    }
                    if (mapSection.isSet("color")) {
                        Color color = parseColor(mapSection.getString("color"));
                        map.setColor(color);
                    }
                }
                if (supports(14)) {
                    ConfigurationSection view = mapSection.getConfigurationSection("view");
                    if (view != null) {
                        World world = Bukkit.getWorld(view.getString("world"));
                        if (world != null) {
                            MapView mapView = Bukkit.createMap(world);
                            mapView.setWorld(world);
                            mapView.setScale(Enums.getIfPresent(MapView.Scale.class, view.getString("scale")).or(MapView.Scale.NORMAL));
                            mapView.setLocked(view.getBoolean("locked"));
                            mapView.setTrackingPosition(view.getBoolean("tracking-position"));
                            mapView.setUnlimitedTracking(view.getBoolean("unlimited-tracking"));
                            ConfigurationSection centerSection = view.getConfigurationSection("center");
                            mapView.setCenterX(centerSection.getInt("x"));
                            mapView.setCenterZ(centerSection.getInt("z"));
                            map.setMapView(mapView);
                        }
                    }
                }
            }
        } else if (supports(17)) {
            if (meta instanceof AxolotlBucketMeta) {
                AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                String variantStr = config.getString("variant");
                if (variantStr != null) {
                    Axolotl.Variant variant = Enums.getIfPresent(Axolotl.Variant.class, variantStr.toUpperCase(Locale.ENGLISH)).or(Axolotl.Variant.BLUE);
                    bucket.setVariant(variant);
                }
            }
        } else if (supports(16)) {
            if (meta instanceof CompassMeta) {
                CompassMeta compass = (CompassMeta) meta;
                compass.setLodestoneTracked(config.getBoolean("tracked"));
                ConfigurationSection lodestone = config.getConfigurationSection("lodestone");
                if (lodestone != null) {
                    World world = Bukkit.getWorld(lodestone.getString("world"));
                    double x = lodestone.getDouble("x");
                    double y = lodestone.getDouble("y");
                    double z = lodestone.getDouble("z");
                    compass.setLodestone(new Location(world, x, y, z));
                }
            }
        } else if (supports(15)) {
            if (meta instanceof SuspiciousStewMeta) {
                SuspiciousStewMeta stew = (SuspiciousStewMeta) meta;
                for (String effects : config.getStringList("effects")) {
                    PotionEffect effect = XPotion.parsePotionEffectFromString(effects);
                    stew.addCustomEffect(effect, true);
                }
            }
        } else if (supports(14)) {
            if (meta instanceof CrossbowMeta) {
                CrossbowMeta crossbow = (CrossbowMeta) meta;
                for (String projectiles : config.getConfigurationSection("projectiles").getKeys(false)) {
                    ItemStack projectile = deserialize(config.getConfigurationSection("projectiles." + projectiles));
                    crossbow.addChargedProjectile(projectile);
                }
            } else if (meta instanceof TropicalFishBucketMeta) {
                TropicalFishBucketMeta tropical = (TropicalFishBucketMeta) meta;
                DyeColor color = Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.WHITE);
                DyeColor patternColor = Enums.getIfPresent(DyeColor.class, config.getString("pattern-color")).or(DyeColor.WHITE);
                TropicalFish.Pattern pattern = Enums.getIfPresent(TropicalFish.Pattern.class, config.getString("pattern")).or(TropicalFish.Pattern.BETTY);
                tropical.setBodyColor(color);
                tropical.setPatternColor(patternColor);
                tropical.setPattern(pattern);
            }
        } else if (!supports(13)) {
            if (supports(11)) {
                if (meta instanceof SpawnEggMeta) {
                    SpawnEggMeta spawnEgg = (SpawnEggMeta) meta;
                    EntityType creature = Enums.getIfPresent(EntityType.class, config.getString("creature").toUpperCase(Locale.ENGLISH)).or(EntityType.BAT);
                    spawnEgg.setSpawnedType(creature);
                }
            } else {
                MaterialData data = item.getData();
                if (data instanceof SpawnEgg) {
                    SpawnEgg spawnEgg = (SpawnEgg) data;
                    EntityType creature = Enums.getIfPresent(EntityType.class, config.getString("creature").toUpperCase(Locale.ENGLISH)).or(EntityType.BAT);
                    spawnEgg.setSpawnedType(creature);
                    item.setData(data);
                }
            }
        }
        String name = config.getString("name");
        if (!Strings.isNullOrEmpty(name)) {
            String translated = ChatColor.translateAlternateColorCodes('&', name);
            meta.setDisplayName(translated);
        } else if (name != null && name.isEmpty()) {
            meta.setDisplayName(" ");
        }
        if (supports(11)) {
            meta.setUnbreakable(config.getBoolean("unbreakable"));
        }
        if (supports(14)) {
            int modelData = config.getInt("model-data");
            if (modelData != 0) {
                meta.setCustomModelData(modelData);
            }
        }
        List<String> loreList = config.getStringList("lore");
        if (!loreList.isEmpty()) {
            List<String> translatedLore = new ArrayList<>(loreList.size());
            String lastColors = "";
            for (String lore : loreList) {
                if (lore.isEmpty()) {
                    translatedLore.add(" ");
                    continue;
                }
                for (String singleLore : StringUtils.splitPreserveAllTokens(lore, '\n')) {
                    if (singleLore.isEmpty()) {
                        translatedLore.add(" ");
                        continue;
                    }
                    singleLore = lastColors + ChatColor.translateAlternateColorCodes('&', singleLore);
                    translatedLore.add(singleLore);
                    lastColors = ChatColor.getLastColors(singleLore);
                }
            }
            meta.setLore(translatedLore);
        } else {
            String lore = config.getString("lore");
            if (!Strings.isNullOrEmpty(lore)) {
                List<String> translatedLore = new ArrayList<>(10);
                String lastColors = "";
                for (String singleLore : StringUtils.splitPreserveAllTokens(lore, '\n')) {
                    if (singleLore.isEmpty()) {
                        translatedLore.add(" ");
                        continue;
                    }
                    singleLore = lastColors + ChatColor.translateAlternateColorCodes('&', singleLore);
                    translatedLore.add(singleLore);
                    lastColors = ChatColor.getLastColors(singleLore);
                }
                meta.setLore(translatedLore);
            }
        }
        ConfigurationSection enchants = config.getConfigurationSection("enchants");
        if (enchants != null) {
            for (String ench : enchants.getKeys(false)) {
                Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(ench);
                enchant.ifPresent(xEnchantment -> meta.addEnchant(xEnchantment.parseEnchantment(), enchants.getInt(ench), true));
            }
        }
        ConfigurationSection enchantment = config.getConfigurationSection("stored-enchants");
        if (enchantment != null) {
            for (String e : enchantment.getKeys(false)) {
                Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(e);
                EnchantmentStorageMeta book = (EnchantmentStorageMeta) meta;
                enchant.ifPresent(xEnchantment -> book.addStoredEnchant(xEnchantment.parseEnchantment(), enchantment.getInt(e), true));
            }
        }
        List<String> flags = config.getStringList("flags");
        for (String flag : flags) {
            flag = flag.toUpperCase(Locale.ENGLISH);
            if (flag.equals("ALL")) {
                meta.addItemFlags(ItemFlag.values());
                break;
            }
            ItemFlag itemFlag = Enums.getIfPresent(ItemFlag.class, flag).orNull();
            if (itemFlag != null) {
                meta.addItemFlags(itemFlag);
            }
        }
        if (supports(13)) {
            ConfigurationSection attributes = config.getConfigurationSection("attributes");
            if (attributes != null) {
                for (String attribute : attributes.getKeys(false)) {
                    Attribute attributeInst = Enums.getIfPresent(Attribute.class, attribute.toUpperCase(Locale.ENGLISH)).orNull();
                    if (attributeInst == null) {
                        continue;
                    }
                    String attribId = attributes.getString("id");
                    UUID id = attribId != null ? UUID.fromString(attribId) : UUID.randomUUID();
                    AttributeModifier modifier = new AttributeModifier(
                            id,
                            attributes.getString("name"),
                            attributes.getInt("amount"),
                            Enums.getIfPresent(AttributeModifier.Operation.class, attributes.getString("operation")).or(AttributeModifier.Operation.ADD_NUMBER),
                            Enums.getIfPresent(EquipmentSlot.class, attributes.getString("slot")).or(EquipmentSlot.HAND));
                    meta.addAttributeModifier(attributeInst, modifier);
                }
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Parses RGB color codes from a string.
     * This only works for 1.13 and above.
     *
     * @param str the RGB string.
     * @return a color based on the RGB.
     * @since 1.1.0
     */
    @Nonnull
    public static Color parseColor(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return Color.BLACK;
        }
        String[] rgb = StringUtils.split(StringUtils.deleteWhitespace(str), ',');
        if (rgb.length < 3) {
            return Color.WHITE;
        }
        return Color.fromRGB(NumberUtils.toInt(rgb[0], 0), NumberUtils.toInt(rgb[1], 0), NumberUtils.toInt(rgb[2], 0));
    }

    public static String untranslate(String str) {
        if (str.indexOf('{') == 0 && str.lastIndexOf('}') == str.length() - 1) {
            try {
                return TextComponent.toPlainText(ComponentSerializer.parse(str));
            } catch (Exception ex) {
                return str;
            }
        }
        return str;
    }

    public static List<String> untranslate(List<String> str) {
        return str.stream().map(XItemStack::untranslate).collect(Collectors.toList());
    }
}
