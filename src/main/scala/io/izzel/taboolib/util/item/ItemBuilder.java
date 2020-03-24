package io.izzel.taboolib.util.item;

import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.lite.Materials;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.List;

/**
 * @Author sky
 * @Since 2018-08-22 11:37
 * @BuilderVersion 1.1
 */
public class ItemBuilder {

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(Material material) {
        this(material, 1, 0);
    }

    public ItemBuilder(Material material, int amount) {
        this(material, amount, 0);
    }

    public ItemBuilder(Material material, int amount, int damage) {
        itemStack = new ItemStack(material, amount, (short) damage);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(OfflinePlayer player) {
        this(Materials.PLAYER_HEAD.parseMaterial(), 1, 3);
        this.skullOwner(player.getName());
    }

    public ItemBuilder material(int id) {
        itemStack.setType(Items.asMaterial(String.valueOf(id)));
        return this;
    }

    public ItemBuilder material(String material) {
        itemStack.setType(Material.getMaterial(material));
        return this;
    }

    public ItemBuilder material(Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder damage(int damage) {
        itemStack.setDurability((short) damage);
        return this;
    }

    public ItemBuilder name(String name) {
        itemMeta.setDisplayName(name);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        itemMeta.setLore(lore);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        itemMeta.setLore(ArrayUtil.asList(lore));
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return enchant(enchantment, level, false);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level, boolean bypass) {
        itemMeta.addEnchant(enchantment, level, bypass);
        return this;
    }

    public ItemBuilder shiny() {
        return enchant(Enchantment.LURE, 1, true).flags(ItemFlag.values());
    }

    public ItemBuilder color(Color color) {
        if (itemMeta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) itemMeta).setColor(color);
        }
        return this;
    }

    public ItemBuilder banner(Pattern... patterns) {
        if (itemMeta instanceof BannerMeta) {
            java.util.Arrays.stream(patterns).forEach(pattern -> ((BannerMeta) itemMeta).addPattern(pattern));
        }
        return this;
    }

    public ItemBuilder potionData(PotionData potionData) {
        if (itemMeta instanceof PotionMeta) {
            ((PotionMeta) itemMeta).setBasePotionData(potionData);
        }
        return this;
    }

    public ItemBuilder potionColor(Color color) {
        if (itemMeta instanceof PotionMeta) {
            ((PotionMeta) itemMeta).setColor(color);
        }
        return this;
    }

    public ItemBuilder potionEffect(PotionEffect potionEffect) {
        if (itemMeta instanceof PotionMeta) {
            ((PotionMeta) itemMeta).addCustomEffect(potionEffect, false);
        }
        return this;
    }

    public ItemBuilder potionEffect(PotionEffect potionEffect, boolean b) {
        if (itemMeta instanceof PotionMeta) {
            ((PotionMeta) itemMeta).addCustomEffect(potionEffect, b);
        }
        return this;
    }

    public ItemBuilder eggType(EntityType entityType) {
        if (itemMeta instanceof SpawnEggMeta) {
            ((SpawnEggMeta) itemMeta).setSpawnedType(entityType);
        }
        return this;
    }

    public ItemBuilder skullOwner(String name) {
        if (itemMeta instanceof SkullMeta) {
            ((SkullMeta) itemMeta).setOwner(name);
        }
        return this;
    }

    public ItemBuilder unbreakable(boolean value) {
        if (Version.isAfter(Version.v1_12)) {
            itemMeta.setUnbreakable(value);
        } else {
            itemMeta.spigot().setUnbreakable(value);
        }
        return this;
    }

    public ItemBuilder colored() {
        if (itemMeta.hasDisplayName()) {
            itemMeta.setDisplayName(TLocale.Translate.setColored(itemMeta.getDisplayName()));
        }
        if (itemMeta.hasLore()) {
            itemMeta.setLore(TLocale.Translate.setColored(itemMeta.getLore()));
        }
        return this;
    }

    public ItemStack build() {
        ItemStack buildItem = itemStack.clone();
        if (itemMeta != null) {
            buildItem.setItemMeta(itemMeta.clone());
        }
        return buildItem;
    }
}
