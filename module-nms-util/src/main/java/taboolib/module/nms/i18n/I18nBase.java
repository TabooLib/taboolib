package taboolib.module.nms.i18n;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 原版语言文件实现接口
 *
 * @author sky
 * @since 2020-04-04 19:42
 */
public abstract class I18nBase {

    abstract public void init();

    /**
     * 获取实体对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player 玩家
     * @param entity 实体
     * @return String
     */
    @NotNull
    abstract public String getName(@Nullable Player player, @NotNull Entity entity);

    /**
     * 获取物品对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player    玩家
     * @param itemStack 物品
     * @return String
     */
    @NotNull
    abstract public String getName(@Nullable Player player, @NotNull ItemStack itemStack);

    /**
     * 获取附魔对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player      玩家
     * @param enchantment 附魔
     * @return String
     */
    @NotNull
    abstract public String getName(@Nullable Player player, @NotNull Enchantment enchantment);

    /**
     * 获取药水效果对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player           玩家
     * @param potionEffectType 药水效果
     * @return String
     */
    @NotNull
    abstract public String getName(@Nullable Player player, @NotNull PotionEffectType potionEffectType);

    /**
     * 获取实体对应中文译名
     *
     * @param entity 实体
     * @return String
     */
    public String getName(@NotNull Entity entity) {
        return getName(null, entity);
    }

    /**
     * 获取物品对应译名
     *
     * @param itemStack 物品
     * @return String
     */
    public String getName(@NotNull ItemStack itemStack) {
        return getName(null, itemStack);
    }

    /**
     * 获取附魔对应中文译名
     *
     * @param enchantment 实体
     * @return String
     */
    public String getName(@NotNull Enchantment enchantment) {
        return getName(null, enchantment);
    }

    /**
     * 获取药水效果对应译名
     *
     * @param potionEffectType 药水效果
     * @return String
     */
    public String getName(@NotNull PotionEffectType potionEffectType) {
        return getName(null, potionEffectType);
    }
}
