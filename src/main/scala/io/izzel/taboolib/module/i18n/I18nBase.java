package io.izzel.taboolib.module.i18n;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 原版语言文件实现接口
 *
 * @Author sky
 * @Since 2020-04-04 19:42
 */
public abstract class I18nBase {

    abstract public void init();

    /**
     * 获取实体对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player 玩家
     * @param entity 实体
     */
    @NotNull
    abstract public String getName(@Nullable Player player, @NotNull Entity entity);

    /**
     * 获取物品对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player    玩家
     * @param itemStack 物品
     */
    @NotNull
    abstract public String getName(@Nullable Player player, @NotNull ItemStack itemStack);

    /**
     * 获取实体对应中文译名
     *
     * @param entity 实体
     */
    public String getName(@NotNull Entity entity) {
        return getName(null, entity);
    }

    /**
     * 获取物品对应译名
     *
     * @param itemStack 物品
     */
    public String getName(@NotNull ItemStack itemStack) {
        return getName(null, itemStack);
    }
}
