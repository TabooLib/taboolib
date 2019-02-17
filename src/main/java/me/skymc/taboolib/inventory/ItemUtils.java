package me.skymc.taboolib.inventory;

import com.ilummc.tlib.resources.TLocale;
import me.clip.placeholderapi.PlaceholderAPI;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.configuration.TConfiguration;
import me.skymc.taboolib.common.function.TFunction;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.itemnbtapi.NBTItem;
import me.skymc.taboolib.itemnbtapi.NBTList;
import me.skymc.taboolib.itemnbtapi.NBTListCompound;
import me.skymc.taboolib.itemnbtapi.NBTType;
import me.skymc.taboolib.other.NumberUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author sky
 */
@TFunction(enable = "init")
public class ItemUtils {

    private static File finalItemsFolder;
    private static FileConfiguration itemDir;
    private static FileConfiguration itemCache;
    private static TConfiguration itemName;
    private static LinkedHashMap<String, String> itemLib = new LinkedHashMap<>();
    private static LinkedHashMap<String, ItemStack> itemCaches = new LinkedHashMap<>();
    private static LinkedHashMap<String, ItemStack> itemCachesFinal = new LinkedHashMap<>();

    public static void init() {
        try {
            reloadItemDir();
            reloadItemName();
            reloadItemCache();
        } catch (Exception e) {
            TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-ITEMS", e.toString());
        }
    }

    public static void reloadItemDir() {
        File file = new File(Main.getInst().getConfig().getString("DATAURL.ITEMDIR"));
        if (file.exists()) {
            itemDir = YamlConfiguration.loadConfiguration(file);
        }
    }

    public static void reloadItemName() {
        itemName = TConfiguration.createInResource(Main.getInst(), "Language/ITEM_NAME.yml");
        itemName.listener(() -> {
            itemName.getConfigurationSection("").getKeys(false).forEach(a -> itemLib.put(a, itemName.getString(a)));
            TabooLib.debug("Loaded " + itemLib.size() + " items name.");
//            TLocale.Logger.info("ITEM-UTILS.SUCCESS-LOAD-NAMES", String.valueOf(itemLib.size()));
        }).runListener();
    }

    public static void reloadItemCache() {
        itemCaches.clear();
        itemCachesFinal.clear();
        loadItemsFile(getItemCacheFile(), false);
        finalItemsFolder = new File(Main.getInst().getDataFolder(), "FinalItems");
        if (!finalItemsFolder.exists()) {
            finalItemsFolder.mkdir();
        }
        Arrays.stream(finalItemsFolder.listFiles()).forEach(file -> loadItemsFile(file, true));
        TabooLib.debug("Loaded " + (itemCaches.size() + itemCachesFinal.size()) + " items.");
//        TLocale.Logger.info("ITEM-UTILS.SUCCESS-LOAD-CACHES", String.valueOf(itemCaches.size() + itemCachesFinal.size()));
    }

    public static File getItemCacheFile() {
        File itemCacheFile = new File(Main.getInst().getDataFolder(), "items.yml");
        if (!itemCacheFile.exists()) {
            Main.getInst().saveResource("items.yml", true);
        }
        return itemCacheFile;
    }

    public static void loadItemsFile(File file, boolean finalFile) {
        FileConfiguration conf = ConfigUtils.load(Main.getInst(), file);
        for (String name : conf.getConfigurationSection("").getKeys(false)) {
            if (isExists(name)) {
                TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-ITEMS", name);
            } else if (finalFile) {
                itemCachesFinal.put(name, loadItem(conf, name));
            } else {
                itemCaches.put(name, loadItem(conf, name));
            }
        }
    }

    // *********************************
    //
    //              API
    //
    // *********************************

    public static boolean isExists(String name) {
        return itemCachesFinal.containsKey(name) || itemCaches.containsKey(name);
    }

    public static ItemStack getCacheItem(String name) {
        return itemCachesFinal.containsKey(name) ? itemCachesFinal.get(name) : itemCaches.get(name);
    }

    public static ItemStack getItemFromDir(String name) {
        return itemDir != null ? itemDir.getItemStack("item." + name) : null;
    }

    public static String getCustomName(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) {
            return TLocale.asString("ITEM-UTILS.EMPTY-ITEM");
        }
        int data = item.getType().getMaxDurability() == 0 ? item.getDurability() : 0;
        return item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : itemLib.get(item.getType() + ":" + data) == null ? item.getType().toString() : itemLib.get(item.getType() + ":" + data);
    }

    public static ItemStack setName(ItemStack i, String n) {
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(n);
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack enchant(ItemStack i, Enchantment e, int l) {
        ItemMeta meta = i.getItemMeta();
        meta.addEnchant(e, l, false);
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack addFlag(ItemStack i, ItemFlag f) {
        ItemMeta meta = i.getItemMeta();
        meta.addItemFlags(f);
        i.setItemMeta(meta);
        return i;
    }

    public static boolean isNull(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    public static boolean isName(ItemStack i, String a) {
        return isNamed(i) && i.getItemMeta() != null && i.getItemMeta().getDisplayName() != null && i.getItemMeta().getDisplayName().equals(a);
    }

    public static boolean isNameAs(ItemStack i, String a) {
        return isNamed(i) && i.getItemMeta().getDisplayName().contains(a);
    }

    public static String asString(String args, Player placeholderPlayer) {
        return placeholderPlayer == null ? args.replace("&", "§") : PlaceholderAPI.setPlaceholders(placeholderPlayer, args.replace("&", "§"));
    }

    public static List<String> asString(List<String> args, Player placeholderPlayer) {
        IntStream.range(0, args.size()).forEach(i -> args.set(i, asString(args.get(i), placeholderPlayer)));
        return args;
    }

    public static ItemFlag asItemFlag(String flag) {
        try {
            return ItemFlag.valueOf(flag);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static Material asMaterial(String args) {
        try {
            Material material = Material.getMaterial(args.toUpperCase());
            return material != null ? material : Material.getMaterial(Integer.valueOf(args));
        } catch (Exception e) {
            return Material.STONE;
        }
    }

    @SuppressWarnings({"deprecation"})
    public static Enchantment asEnchantment(String enchant) {
        try {
            Enchantment enchantment = Enchantment.getByName(enchant);
            return enchantment != null ? enchantment : Enchantment.getById(Integer.valueOf(enchant));
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static PotionEffectType asPotionEffectType(String potion) {
        try {
            PotionEffectType type = PotionEffectType.getByName(potion);
            return type != null ? type : PotionEffectType.getById(Integer.valueOf(potion));
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

    public static int getLore(ItemStack i, String a) {
        return isLored(i) ? IntStream.range(0, i.getItemMeta().getLore().size()).filter(j -> i.getItemMeta().getLore().get(j).contains(a)).findFirst().orElse(0) : 0;
    }

    public static boolean hasLore(ItemStack i, String a) {
        return isLored(i) && i.getItemMeta().getLore().toString().contains(a);
    }

    public static boolean isLored(ItemStack i) {
        return i != null && i.getItemMeta() != null && i.getItemMeta().getLore() != null;
    }

    public static boolean isNamed(ItemStack i) {
        return i != null && i.getItemMeta() != null && i.getItemMeta().getDisplayName() != null;
    }

    public static ItemStack addLore(ItemStack is, String line) {
        ItemMeta meta = is.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : Collections.emptyList();
        lore.add(TLocale.Translate.setColored(line));
        is.setItemMeta(meta);
        return is;
    }

    public static ItemStack delLore(ItemStack is, int line) {
        ItemMeta meta = is.getItemMeta();
        if (meta.hasLore()) {
            List<String> l = meta.getLore();
            if (l.size() >= line) {
                l.remove(line);
                meta.setLore(l);
                is.setItemMeta(meta);
            }
        }
        return is;
    }

    public static ItemStack replaceLore(ItemStack i, String l1, String l2) {
        if (!isLored(i)) {
            return i;
        } else {
            ItemMeta meta = i.getItemMeta();
            List<String> lore = meta.getLore();
            IntStream.range(0, lore.size()).forEach(j -> lore.set(j, lore.get(j).replace(l1, l2)));
            meta.setLore(lore);
            i.setItemMeta(meta);
        }
        return i;
    }

    public static ItemStack addDurability(ItemStack i, int d) {
        i.setDurability((short) (i.getDurability() + d));
        int min = i.getDurability();
        int max = i.getType().getMaxDurability();
        if (min >= max) {
            i.setType(Material.AIR);
        }
        return i;
    }

    public static ItemStack loadItem(FileConfiguration f, String s) {
        return loadItem(f, s, null);
    }

    public static ItemStack loadItem(FileConfiguration f, String s, Player papiPlayer) {
        return loadItem(f.getConfigurationSection(s), papiPlayer);
    }

    public static ItemStack loadItem(ConfigurationSection section, Player papiPlayer) {
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
            meta.setDisplayName(asString(section.getString("name"), papiPlayer));
        }
        // 描述
        if (section.contains("lore")) {
            meta.setLore(asString(section.getStringList("lore"), papiPlayer));
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
        if (section.contains("flags") && TabooLib.getVerint() > 10700) {
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
                            NumberUtils.getInteger(section.getString("potions." + prePotionName).split("-")[0]),
                            NumberUtils.getInteger(section.getString("potions." + prePotionName).split("-")[1]) - 1), true);
                } else {
                    TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-POTION", prePotionName);
                }
            }
        }
        // 元数据
        item.setItemMeta(meta);
        // 数据
        NBTItem nbt = new NBTItem(item);
        // 物品标签
        if (section.contains("nbt")) {
            for (String name : section.getConfigurationSection("nbt").getKeys(false)) {
                Object obj = section.get("nbt." + name);
                if (obj instanceof String) {
                    nbt.setString(name, obj.toString());
                } else if (obj instanceof Double) {
                    nbt.setDouble(name, Double.valueOf(obj.toString()));
                } else if (obj instanceof Integer) {
                    nbt.setInteger(name, Integer.valueOf(obj.toString()));
                } else if (obj instanceof Long) {
                    nbt.setLong(name, Long.valueOf(obj.toString()));
                } else {
                    nbt.setObject(name, obj);
                }
            }
        }
        // 物品属性
        if (section.contains("attributes")) {
            NBTList attr = nbt.getList("AttributeModifiers", NBTType.NBTTagCompound);
            for (String hand : section.getConfigurationSection("attributes").getKeys(false)) {
                for (String name : section.getConfigurationSection("attributes." + hand).getKeys(false)) {
                    if (asAttribute(name) != null) {
                        try {
                            NBTListCompound _attr = attr.addCompound();
                            Object num = section.get("attributes." + hand + "." + name);
                            if (num.toString().contains("%")) {
                                _attr.setDouble("Amount", Double.valueOf(num.toString().replace("%", "")) / 100D);
                                _attr.setInteger("Operation", 1);
                            } else {
                                _attr.setDouble("Amount", Double.valueOf(num.toString()));
                                _attr.setInteger("Operation", 0);
                            }
                            _attr.setString("AttributeName", asAttribute(name));
                            _attr.setInteger("UUIDMost", NumberUtils.getRandom().nextInt(Integer.MAX_VALUE));
                            _attr.setInteger("UUIDLeast", NumberUtils.getRandom().nextInt(Integer.MAX_VALUE));
                            _attr.setString("Name", asAttribute(name));
                            if (!"all".equals(hand)) {
                                _attr.setString("Slot", hand);
                            }
                        } catch (Exception ignored) {
                        }
                    } else {
                        TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-POTION", name);
                    }
                }
            }
        }
        return nbt.getItem();
    }

    public static NBTItem setAttribute(NBTItem nbt, String name, Object num, String hand) {
        NBTList attr = nbt.getList("AttributeModifiers", NBTType.NBTTagCompound);
        if (asAttribute(name) != null) {
            try {
                NBTListCompound _attr = null;
                for (int i = 0; i < attr.size(); i++) {
                    NBTListCompound nlc = attr.getCompound(i);
                    if (nlc.getString("AttributeName").equals(asAttribute(name))) {
                        _attr = nlc;
                    }
                }
                if (_attr == null) {
                    _attr = attr.addCompound();
                }
                if (num.toString().contains("%")) {
                    _attr.setDouble("Amount", Double.valueOf(num.toString().replace("%", "")) / 100D);
                    _attr.setInteger("Operation", 1);
                } else {
                    _attr.setDouble("Amount", Double.valueOf(num.toString()));
                    _attr.setInteger("Operation", 0);
                }
                _attr.setString("AttributeName", asAttribute(name));
                _attr.setInteger("UUIDMost", NumberUtils.getRandom().nextInt(Integer.MAX_VALUE));
                _attr.setInteger("UUIDLeast", NumberUtils.getRandom().nextInt(Integer.MAX_VALUE));
                _attr.setString("Name", asAttribute(name));
                if (!"all".equals(hand)) {
                    _attr.setString("Slot", hand);
                }
            } catch (NumberFormatException ignored) {
            }
        } else {
            TLocale.Logger.error("ITEM-UTILS.FAIL-LOAD-POTION", name);
        }
        return nbt;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static FileConfiguration getItemDir() {
        return itemDir;
    }

    public static LinkedHashMap<String, String> getItemLib() {
        return itemLib;
    }

    public static FileConfiguration getItemCache() {
        return itemCache;
    }

    public static File getFinalItemsFolder() {
        return finalItemsFolder;
    }

    public static LinkedHashMap<String, ItemStack> getItemCaches() {
        return itemCaches;
    }

    public static LinkedHashMap<String, ItemStack> getItemCachesFinal() {
        return itemCachesFinal;
    }

    // *********************************
    //
    //        Deprecated
    //
    // *********************************

    @Deprecated
    public static FileConfiguration getItemdir() {
        return itemDir;
    }

    @Deprecated
    public static LinkedHashMap<String, String> getItemlib() {
        return itemLib;
    }

    @Deprecated
    public static ItemStack item(int n, int a, int d) {
        return new ItemStack(n, a, (short) d);
    }

    @Deprecated
    public static ItemStack repalceLore(ItemStack i, String l1, String l2) {
        return replaceLore(i, l1, l2);
    }

    @Deprecated
    public static void putO(ItemStack item, Inventory inv, int i) {
        inv.setItem(i, item);
        inv.setItem(i + 1, item);
        inv.setItem(i + 2, item);
        inv.setItem(i + 9, item);
        inv.setItem(i + 10, null);
        inv.setItem(i + 11, item);
        inv.setItem(i + 18, item);
        inv.setItem(i + 19, item);
        inv.setItem(i + 20, item);
    }
}
