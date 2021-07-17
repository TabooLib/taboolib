package taboolib.module.nms.i18n;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.env.RuntimeResource;
import taboolib.module.nms.ItemTag;
import taboolib.module.nms.MinecraftVersion;
import taboolib.module.nms.NMSKt;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

/**
 * @author sky
 * @since 2020-04-04 19:44
 */
@RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/i18n_high_zh_CN.yml",
        hash = "3aa002f314ac0768b00daa3563c08da9b5c674c5",
        zip = true
)
@RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/i18n_low_zh_CN.yml",
        hash = "476754933e2e486048f39726b822f24447d60ecb",
        zip = true
)
public class I18nOrigin extends I18nBase {

    public static final I18nOrigin INSTANCE = new I18nOrigin();

    private static FileConfiguration lang;

    @Override
    public void init() {
        lang = YamlConfiguration.loadConfiguration(getLocaleFile());
    }

    @Override
    public @NotNull
    String getName(Player player, @NotNull Entity entity) {
        return Objects.requireNonNull(lang.getString(NMSKt.getInternalName(entity).replace(".", "_"), entity.getName()));
    }

    @Override
    public @NotNull
    String getName(Player player, @NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof BookMeta && ((BookMeta) itemMeta).getTitle() != null) {
            return Objects.requireNonNull(((BookMeta) itemMeta).getTitle());
        }
        // < 1.11
        if (MinecraftVersion.INSTANCE.getMajor() < 3) {
            if (itemStack.getType().name().equals("MONSTER_EGG")) {
                ItemTag itemTag = NMSKt.getItemTag(itemStack);
                if (itemTag.containsKey("EntityTag")) {
                    return lang.getString("item_monsterPlacer_name") + " " + lang.getString("entity_" + itemTag.get("EntityTag").asCompound().get("id").asString() + "_name");
                }
                return Objects.requireNonNull(lang.getString("item_monsterPlacer_name"));
            }
        }
        // < 1.13
        else if (MinecraftVersion.INSTANCE.getMajor() < 5) {
            if (itemMeta instanceof SpawnEggMeta) {
                String spawnEggType = lang.getString("entity_" + ((SpawnEggMeta) itemMeta).getSpawnedType().getEntityClass().getSimpleName().replace(".", "_") + "_name");
                if (spawnEggType != null) {
                    return lang.getString(NMSKt.getInternalName(itemStack).replace(".", "_"), itemStack.getType().name().toLowerCase(Locale.getDefault()).replace("_", "")) + " " + spawnEggType;
                }
            }
        }
        if (itemStack.getType().equals(Material.AIR)) {
            return Objects.requireNonNull(lang.getString("item_air"));
        } else {
            return Objects.requireNonNull(lang.getString(NMSKt.getInternalName(itemStack).replace(".", "_"), itemStack.getType().name().toLowerCase(Locale.getDefault()).replace("_", "")));
        }
    }

    @Override
    public @NotNull
    String getName(@Nullable Player player, @NotNull Enchantment enchantment) {
        return enchantment.getName();
    }

    @Override
    public @NotNull
    String getName(@Nullable Player player, @NotNull PotionEffectType potionEffectType) {
        return potionEffectType.getName();
    }

    private File getLocaleFile() {
        if (MinecraftVersion.INSTANCE.getMajor() >= 5) {
            return new File("assets/3a/3aa002f314ac0768b00daa3563c08da9b5c674c5");
        } else {
            return new File("assets/47/476754933e2e486048f39726b822f24447d60ecb");
        }
    }
}
