package io.izzel.taboolib.util.item;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.cronus.CronusUtils;
import io.izzel.taboolib.module.i18n.I18n;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 物品操作工具
 *
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

    /**
     * 获取物品名称，当该物品没有 DisplayName 时则获取中文译名
     *
     * @param item 物品实例
     */
    @NotNull
    public static String getName(ItemStack item) {
        return I18n.get().getName(item);
    }

    /**
     * 物品是否为 null 或 AIR
     *
     * @param item 物品实例
     */
    public static boolean isNull(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    /**
     * 物品不为 null 或 AIR
     *
     * @param item 物品实例
     */
    public static boolean nonNull(ItemStack item) {
        return !isNull(item);
    }

    /**
     * 物品是否拥有 DisplayName
     *
     * @param i 物品实例
     */
    public static boolean hasName(ItemStack i) {
        return !isNull(i) && i.getItemMeta().hasDisplayName();
    }

    /**
     * 物品名称是否含有特定关键字（含 DisplayName 和中文译名）
     *
     * @param i 物品实例
     * @param a 关键字
     */
    public static boolean hasName(ItemStack i, String a) {
        return hasName(i) && getName(i).contains(a);
    }

    /**
     * 物品描述是否含有特定关键字
     *
     * @param i 物品实例
     * @param a 关键字
     */
    public static boolean hasLore(ItemStack i, String a) {
        return hasLore(i) && i.getItemMeta().getLore().toString().contains(a);
    }

    /**
     * 物品是否含有描述
     *
     * @param i 物品实例
     */
    public static boolean hasLore(ItemStack i) {
        return !isNull(i) && i.getItemMeta().hasLore();
    }

    /**
     * 通过文本获取 Material 枚举，获取失败会返回 STONE 类型
     *
     * @param args 文本
     */
    @Nullable
    public static Material asMaterial(String args) {
        if (CronusUtils.isInt(args)) {
            try {
                return Material.getMaterial(NumberConversions.toInt(args));
            } catch (Throwable ignored) {
                return XMaterial.matchXMaterial(NumberConversions.toInt(args), (byte) -1).orElse(XMaterial.STONE).parseMaterial();
            }
        } else {
            return XMaterial.matchXMaterial(args.toUpperCase()).orElse(XMaterial.STONE).parseMaterial();
        }
    }

    /**
     * 通过文本获取 Color 类型，格式为 R-G-B，获取失败会返回 0-0-0
     *
     * @param color 文本
     */
    @NotNull
    public static Color asColor(String color) {
        try {
            String[] v = color.split("-");
            return Color.fromRGB(NumberConversions.toInt(v[0]), NumberConversions.toInt(v[1]), NumberConversions.toInt(v[2]));
        } catch (Throwable e) {
            return Color.fromRGB(0, 0, 0);
        }
    }

    /**
     * 通过文本获取 Enchantment 类型
     *
     * @param enchant 文本
     */
    @Nullable
    public static Enchantment asEnchantment(String enchant) {
        try {
            Enchantment enchantment = Enchantment.getByName(enchant);
            return enchantment != null ? enchantment : Enchantment.getById(NumberConversions.toInt(enchant));
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 通过文本获取 PotionEffectType 类型
     *
     * @param potion 文本
     */
    @Nullable
    public static PotionEffectType asPotionEffectType(String potion) {
        try {
            PotionEffectType type = PotionEffectType.getByName(potion);
            return type != null ? type : PotionEffectType.getById(NumberConversions.toInt(potion));
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 通过文本获取 ItemFlag 枚举
     *
     * @param flag 文本
     */
    @Nullable
    public static ItemFlag asItemFlag(String flag) {
        return Enums.getIfPresent(ItemFlag.class, flag).orNull();
    }

    /**
     * 通过文本获取 Attribute 的 Minecraft Key
     *
     * @param name 文本
     */
    @Nullable
    public static String asAttribute(String name) {
        Attribute attribute = Attribute.parse(name);
        return attribute == null ? null : attribute.getMinecraftKey();
    }

    /**
     * 替换物品名称（完全替换）
     *
     * @param item    物品实例
     * @param nameOld 文本
     * @param nameNew 文本
     */
    @NotNull
    public static ItemStack replaceName(ItemStack item, String nameOld, String nameNew) {
        return replaceName(item, ImmutableMap.of(nameOld, nameNew));
    }

    /**
     * 替换物品描述（完全替换）
     *
     * @param item    物品实例
     * @param loreOld 文本
     * @param loreNew 文本
     */
    @NotNull
    public static ItemStack replaceLore(ItemStack item, String loreOld, String loreNew) {
        return replaceLore(item, ImmutableMap.of(loreOld, loreNew));
    }

    /**
     * 替换物品名称（完全替换）
     *
     * @param item 物品实例
     * @param map  文本关系
     */
    @NotNull
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

    /**
     * 替换物品描述（完全替换）
     *
     * @param item 物品实例
     * @param map  文本关系
     */
    @NotNull
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

    /**
     * 检查玩家背包中的特定物品是否达到特定数量
     *
     * @param player 玩家
     * @param item   物品
     * @param amount 检查数量
     * @param remove 是否移除
     */
    public static boolean checkItem(Player player, ItemStack item, int amount, boolean remove) {
        return remove ? takeItem(player.getInventory(), i -> i.isSimilar(item), amount) : hasItem(player.getInventory(), i -> i.isSimilar(item), amount);
    }

    /**
     * 检查背包中的特定物品是否达到特定数量
     *
     * @param inventory 背包实例
     * @param item      物品
     * @param amount    检查数量
     * @param remove    是否移除
     */
    public static boolean checkItem(Inventory inventory, ItemStack item, int amount, boolean remove) {
        return hasItem(inventory, i -> i.isSimilar(item), amount) && (!remove || takeItem(inventory, i -> i.isSimilar(item), amount));
    }

    /**
     * 检查背包中符合特定规则的物品是否达到特定该数量
     *
     * @param inventory 背包实例
     * @param matcher   规则
     * @param amount    数量
     */
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

    /**
     * 移除背包中特定数量的符合特定规则的物品
     *
     * @param inventory 背包实例
     * @param matcher   规则
     * @param amount    实例
     */
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

    /**
     * 通过配置文件载入物品实例
     *
     * @param section 配置文件
     */
    @NotNull
    public static ItemStack loadItem(ConfigurationSection section) {
        if (section.get("bukkit") instanceof ItemStack) {
            return section.getItemStack("bukkit");
        }
        // 材质
        ItemStack item = new ItemStack(Objects.requireNonNull(asMaterial(section.contains("material") ? section.getString("material") : section.getString("type"))));
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
                                Optional.ofNullable(Equipments.fromNMS(hand)).ifPresent(e -> a.put("Slot", new NBTBase(e.getNMS())));
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

    /**
     * 通过 Json 读取物品实例
     *
     * @param item json 文本
     */
    @Nullable
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

    /**
     * 将物品转换为 Json 格式
     *
     * @param item 物品实例
     */
    @NotNull
    public static String toJson(ItemStack item) {
        JsonObject json = new JsonObject();
        json.addProperty("type", item.getType().name());
        json.addProperty("data", item.getData().getData());
        json.addProperty("amount", item.getAmount());
        json.add("meta", new JsonParser().parse(NMS.handle().loadNBT(item).toJson()));
        return json.toString();
    }

    /**
     * 将物品转换为 Json 格式（通过格式化）
     *
     * @param item 物品实例
     */
    @NotNull
    public static String toJsonFormatted(ItemStack item) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(new Gson().toJsonTree(toJson(item)));
    }

    /**
     * 物品匹配规则
     */
    public interface Matcher {

        /**
         * 是否符合规则
         *
         * @param item 物品实例
         */
        boolean match(@NotNull ItemStack item);
    }
}
