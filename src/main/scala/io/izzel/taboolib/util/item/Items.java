package io.izzel.taboolib.util.item;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.lite.SimpleEquip;
import io.izzel.taboolib.module.lite.SimpleI18n;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.nms.nbt.Attribute;
import io.izzel.taboolib.module.nms.nbt.NBTBase;
import io.izzel.taboolib.module.nms.nbt.NBTCompound;
import io.izzel.taboolib.module.nms.nbt.NBTList;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    public static boolean nonNull(ItemStack item) {
        return !isNull(item);
    }

    public static boolean hasName(ItemStack i) {
        return !isNull(i) && i.getItemMeta().hasDisplayName();
    }

    public static boolean hasName(ItemStack i, String a) {
        return hasName(i) && getName(i).contains(a);
    }

    public static boolean hasLore(ItemStack i, String a) {
        return hasLore(i) && i.getItemMeta().getLore().toString().contains(a);
    }

    public static boolean hasLore(ItemStack i) {
        return !isNull(i) && i.getItemMeta().hasLore();
    }

    public static Material asMaterial(String args) {
        try {
            Material material = Material.getMaterial(args.toUpperCase());
            return material != null ? material : Material.getMaterial(Integer.valueOf(args));
        } catch (Exception e) {
            return Material.STONE;
        }
    }

    public static Color asColor(String color) {
        try {
            String[] v = color.split("-");
            return Color.fromBGR(Integer.valueOf(v[0]), Integer.valueOf(v[1]), Integer.valueOf(v[2]));
        } catch (Exception e) {
            return Color.fromBGR(0, 0, 0);
        }
    }

    public static ItemFlag asItemFlag(String flag) {
        try {
            return ItemFlag.valueOf(flag);
        } catch (Exception e) {
            return null;
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

    public static String asAttribute(String name) {
        return Attribute.parse(name).getMinecraftKey();
    }

    public static ItemStack replaceName(ItemStack item, String nameOld, String nameNew) {
        return replaceName(item, ImmutableMap.of(nameOld, nameNew));
    }

    public static ItemStack replaceLore(ItemStack item, String loreOld, String loreNew) {
        return replaceLore(item, ImmutableMap.of(loreOld, loreNew));
    }

    public static ItemStack replaceName(ItemStack item, Map<String, String> map) {
        if (hasName(item)) {
            ItemMeta meta = item.getItemMeta();
            String name = meta.getDisplayName();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                name = name.replace(entry.getKey(), entry.getValue());
            }
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack replaceLore(ItemStack item, Map<String, String> map) {
        if (hasLore(item)) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                lore.set(i, line);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean checkItem(Player player, ItemStack item, int amount, boolean remove) {
        return remove ? takeItem(player.getInventory(), i -> i.isSimilar(item), amount) : hasItem(player.getInventory(), i -> i.isSimilar(item), amount);
    }

    public static boolean checkItem(Inventory inventory, ItemStack item, int amount, boolean remove) {
        return hasItem(inventory, i -> i.isSimilar(item), amount) && (!remove || takeItem(inventory, i -> i.isSimilar(item), amount));
    }

    public static boolean hasItem(Inventory inventory, Matcher matcher, int amount) {
        int checkAmount = amount;
        for (ItemStack itemStack : inventory.getContents()) {
            if (!isNull(itemStack) && matcher.match(itemStack)) {
                checkAmount -= itemStack.getAmount();
                if (checkAmount <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean takeItem(Inventory inventory, Matcher matcher, int amount) {
        int takeAmount = amount;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (!isNull(itemStack) && matcher.match(itemStack)) {
                takeAmount -= itemStack.getAmount();
                if (takeAmount < 0) {
                    itemStack.setAmount(itemStack.getAmount() - (takeAmount + itemStack.getAmount()));
                    return true;
                } else {
                    inventory.setItem(i, null);
                    if (takeAmount == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ItemStack loadItem(ConfigurationSection section) {
        if (section.get("bukkit") instanceof ItemStack) {
            return section.getItemStack("bukkit");
        }
        // 材质
        ItemStack item = new ItemStack(asMaterial(section.contains("material") ? section.getString("material") : section.getString("type")));
        // 数量
        item.setAmount(section.contains("amount") ? section.getInt("amount") : section.getInt("count", 1));
        // 耐久
        item.setDurability((short) (section.contains("data") ? section.getInt("data") : section.getInt("damage")));
        // 元数据
        ItemMeta meta = item.getItemMeta();
        // 展示名
        if (section.contains("name")) {
            meta.setDisplayName(TLocale.Translate.setColored(section.getString("name")));
        }
        // 描述
        if (section.contains("lore")) {
            meta.setLore(TLocale.Translate.setColored(section.getStringList("lore")));
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
        NBTCompound nbt = NMS.handle().loadNBT(item);
        // 物品标签
        if (section.contains("nbt")) {
            NBTBase.translateSection(nbt, section.getConfigurationSection("nbt"));
        }
        // 物品属性
        if (section.contains("attributes")) {
            NBTList attr = new NBTList();
            for (String hand : section.getConfigurationSection("attributes").getKeys(false)) {
                for (String name : section.getConfigurationSection("attributes." + hand).getKeys(false)) {
                    if (asAttribute(name) != null) {
                        try {
                            UUID uuid = UUID.randomUUID();
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
                            a.put("UUIDMost", new NBTBase(uuid.getMostSignificantBits()));
                            a.put("UUIDLeast", new NBTBase(uuid.getLeastSignificantBits()));
                            a.put("Name", new NBTBase(asAttribute(name)));
                            if (!hand.equals("all")) {
                                Optional.ofNullable(SimpleEquip.fromNMS(hand)).ifPresent(e -> a.put("Slot", new NBTBase(e.getNMS())));
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
        return NMS.handle().saveNBT(item, nbt);
    }

    public static ItemStack fromJson(String item) {
        JsonElement json = new JsonParser().parse(item);
        if (json instanceof JsonObject) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.STONE);
            JsonElement type = ((JsonObject) json).get("type");
            if (type != null) {
                itemBuilder.material(Items.asMaterial(type.getAsString()));
            }
            JsonElement data = ((JsonObject) json).get("data");
            if (data != null) {
                itemBuilder.damage(data.getAsInt());
            }
            JsonElement amount = ((JsonObject) json).get("amount");
            if (amount != null) {
                itemBuilder.amount(amount.getAsInt());
            }
            ItemStack itemBuild = itemBuilder.build();
            JsonElement meta = ((JsonObject) json).get("meta");
            if (meta != null) {
                return NMS.handle().saveNBT(itemBuild, NBTCompound.fromJson(meta.toString()));
            }
            return itemBuild;
        }
        return null;
    }

    public static String toJson(ItemStack item) {
        JsonObject json = new JsonObject();
        json.addProperty("type", item.getType().name());
        json.addProperty("data", item.getData().getData());
        json.addProperty("amount", item.getAmount());
        json.add("meta", new JsonParser().parse(NMS.handle().loadNBT(item).toJson()));
        return json.toString();
    }

    public static String toJsonFormatted(ItemStack item) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(new Gson().toJsonTree(toJson(item)));
    }

    public interface Matcher {

        boolean match(ItemStack item);
    }
}
