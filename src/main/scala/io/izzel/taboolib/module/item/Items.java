package io.izzel.taboolib.module.item;

import io.izzel.taboolib.Version;
import io.izzel.taboolib.locale.TLocale;
import io.izzel.taboolib.module.lite.SimpleI18n;
import io.izzel.taboolib.module.nms.NMSHandler;
import io.izzel.taboolib.module.nms.nbt.NBTBase;
import io.izzel.taboolib.module.nms.nbt.NBTCompound;
import io.izzel.taboolib.module.nms.nbt.NBTList;
import io.izzel.taboolib.origin.lite.Numbers;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;

import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-07-05 16:44
 */
public class Items {

    public final static Integer[] INVENTORY_CENTER = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static String getName(ItemStack item) {
        return SimpleI18n.getCustomName(item);
    }

    public static boolean isNull(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    public static boolean hasLore(ItemStack i, String a) {
        return hasLore(i) && i.getItemMeta().getLore().toString().contains(a);
    }

    public static boolean hasLore(ItemStack i) {
        return !isNull(i) && i.getItemMeta().hasLore();
    }

    public static boolean hasName(ItemStack i) {
        return !isNull(i) && i.getItemMeta().hasDisplayName();
    }

    public static Material asMaterial(String args) {
        try {
            Material material = Material.getMaterial(args.toUpperCase());
            return material != null ? material : Material.getMaterial(Integer.valueOf(args));
        } catch (Exception e) {
            return Material.STONE;
        }
    }

    public static Enchantment asEnchantment(String enchant) {
        try {
            Enchantment enchantment = Enchantment.getByName(enchant);
            return enchantment != null ? enchantment : Enchantment.getById(Integer.valueOf(enchant));
        } catch (Exception e) {
            return null;
        }
    }

    public static PotionEffectType asPotionEffectType(String potion) {
        try {
            PotionEffectType type = PotionEffectType.getByName(potion);
            return type != null ? type : PotionEffectType.getById(Integer.valueOf(potion));
        } catch (Exception e) {
            return null;
        }
    }

    public static ItemFlag asItemFlag(String flag) {
        try {
            return ItemFlag.valueOf(flag);
        } catch (Exception e) {
            return null;
        }
    }

    public static Color asColor(String color) {
        try {
            return Color.fromBGR(Integer.valueOf(color.split("-")[0]), Integer.valueOf(color.split("-")[1]), Integer.valueOf(color.split("-")[2]));
        } catch (Exception e) {
            return Color.fromBGR(0, 0, 0);
        }
    }

    public static String asAttribute(String name) {
        switch (name.toLowerCase()) {
            case "damage":
                return "generic.attackDamage";
            case "attackspeed":
                return "generic.attackSpeed";
            case "health":
                return "generic.maxHealth";
            case "speed":
                return "generic.movementSpeed";
            case "knockback":
                return "generic.knockbackResistance";
            case "armor":
                return "generic.armor";
            case "luck":
                return "generic.luck";
            default:
                return null;
        }
    }

    public static ItemStack replaceLore(ItemStack item, String loreOld, String loreNew) {
        if (hasLore(item)) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, lore.get(i).replace(loreOld, loreNew));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean checkItem(Player player, ItemStack item, int amount, boolean remove) {
        return checkItem(player.getInventory(), item, amount, remove);
    }

    public static boolean checkItem(Inventory inventory, ItemStack item, int amount, boolean remove) {
        int hasAmount = 0;
        for (ItemStack _item : inventory) {
            if (item.isSimilar(_item)) {
                hasAmount += _item.getAmount();
            }
        }
        if (hasAmount < amount) {
            return false;
        }
        int requireAmount = amount;
        for (int i = 0; i < inventory.getSize() && remove; i++) {
            ItemStack _item = inventory.getItem(i);
            if (_item != null && _item.isSimilar(item)) {
                if (_item.getAmount() < requireAmount) {
                    inventory.setItem(i, null);
                    requireAmount -= _item.getAmount();
                } else if (_item.getAmount() == requireAmount) {
                    inventory.setItem(i, null);
                    return true;
                } else {
                    _item.setAmount(_item.getAmount() - requireAmount);
                    return true;
                }
            }
        }
        return true;
    }

    public static ItemStack loadItem(ConfigurationSection section) {
        if (section.get("bukkit") instanceof ItemStack) {
            return section.getItemStack("bukkit");
        }
        // 材质
        ItemStack item = new ItemStack(asMaterial(section.getString("material")));
        // 数量
        item.setAmount(section.contains("amount") ? section.getInt("amount") : 1);
        // 耐久
        item.setDurability((short) section.getInt("data"));
        // 元数据
        ItemMeta meta = item.getItemMeta();
        // 展示名
        if (section.contains("name")) {
            meta.setDisplayName(section.getString("name"));
        }
        // 描述
        if (section.contains("lore")) {
            meta.setLore(section.getStringList("lore"));
        }
        // 附魔
        if (section.contains("enchant")) {
            for (String preEnchant : section.getConfigurationSection("enchant").getKeys(false)) {
                Enchantment enchant = asEnchantment(preEnchant);
                if (enchant != null) {
                    meta.addEnchant(enchant, section.getInt("enchant." + preEnchant), true);
                } else {
                    TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-ENCHANTS", preEnchant);
                }
            }
        }
        // 标签
        if (section.contains("flags") && Version.isAfter(Version.v1_8)) {
            for (String preFlag : section.getStringList("flags")) {
                ItemFlag flag = asItemFlag(preFlag);
                if (flag != null) {
                    meta.addItemFlags(flag);
                } else {
                    TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-FLAG", preFlag);
                }
            }
        }
        // 皮革
        if (meta instanceof LeatherArmorMeta && section.contains("color")) {
            ((LeatherArmorMeta) meta).setColor(asColor(section.getString("color")));
        }
        // 药水
        if (meta instanceof PotionMeta && section.contains("potions")) {
            PotionMeta potionMeta = (PotionMeta) meta;
            for (String prePotionName : section.getConfigurationSection("potions").getKeys(false)) {
                PotionEffectType potionEffectType = asPotionEffectType(prePotionName);
                if (potionEffectType != null) {
                    potionMeta.addCustomEffect(new PotionEffect(
                            potionEffectType,
                            NumberConversions.toInt(section.getString("potions." + prePotionName).split("-")[0]),
                            NumberConversions.toInt(section.getString("potions." + prePotionName).split("-")[1]) - 1), true);
                } else {
                    TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-POTION", prePotionName);
                }
            }
        }
        // 元数据
        item.setItemMeta(meta);
        // 数据
        NBTCompound nbt = NMSHandler.getHandler().loadNBT(item);
        // 物品标签
        if (section.contains("nbt")) {
            for (String name : section.getConfigurationSection("nbt").getKeys(false)) {
                Object obj = section.get("nbt." + name);
                if (obj instanceof String) {
                    nbt.put(name, new NBTBase(obj.toString()));
                } else if (obj instanceof Double) {
                    nbt.put(name, new NBTBase(NumberConversions.toDouble(obj)));
                } else if (obj instanceof Integer) {
                    nbt.put(name, new NBTBase(NumberConversions.toInt(obj)));
                } else if (obj instanceof Long) {
                    nbt.put(name, new NBTBase(NumberConversions.toLong(obj)));
                }
            }
        }
        // 物品属性
        if (section.contains("attributes")) {
            NBTList attr = new NBTList();
            for (String hand : section.getConfigurationSection("attributes").getKeys(false)) {
                for (String name : section.getConfigurationSection("attributes." + hand).getKeys(false)) {
                    if (asAttribute(name) != null) {
                        try {
                            NBTCompound a = new NBTCompound();
                            String num = section.getString("attributes." + hand + "." + name);
                            if (num.endsWith("%")) {
                                a.put("Amount", new NBTBase(NumberConversions.toDouble(num.substring(0, num.length() - 1)) / 100D));
                                a.put("Operation", new NBTBase(1));
                            } else {
                                a.put("Amount", new NBTBase(NumberConversions.toDouble(num)));
                                a.put("Operation", new NBTBase(0));
                            }
                            a.put("AttributeName", new NBTBase(asAttribute(name)));
                            a.put("UUIDMost", new NBTBase(Numbers.getRandom().nextInt(Integer.MAX_VALUE)));
                            a.put("UUIDLeast", new NBTBase(Numbers.getRandom().nextInt(Integer.MAX_VALUE)));
                            a.put("Name", new NBTBase(asAttribute(name)));
                            if (!hand.equals("all")) {
                                a.put("Slot", new NBTBase(hand));
                            }
                            attr.add(a);
                        } catch (Exception ignored) {
                        }
                    } else {
                        TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-POTION", name);
                    }
                }
            }
            nbt.put("AttributeModifiers", attr);
        }
        return NMSHandler.getHandler().saveNBT(item, nbt);
    }
}
