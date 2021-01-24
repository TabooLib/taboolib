package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import org.bukkit.entity.Player;

/**
 * Vault 支持
 *
 * @author 坏黑
 * @since 2019-07-05 18:50
 */
public class PermissionHook {

    /**
     * 赋予权限
     *
     * @param player 玩家
     * @param perm   权限
     */
    public static void add(Player player, String perm) {
        InternalPluginBridge.handle().permissionAdd(player, perm);
    }

    /**
     * 移除权限
     *
     * @param player 玩家
     * @param perm   权限
     */
    public static void remove(Player player, String perm) {
        InternalPluginBridge.handle().permissionAdd(player, perm);
    }

    /**
     * 判定是否持有权限
     *
     * @param player 玩家
     * @param perm   权限
     * @return boolean
     */
    public static boolean has(Player player, String perm) {
        return InternalPluginBridge.handle().permissionHas(player, perm);
    }

    /**
     * @return 是否支持
     */
    public static boolean exists() {
        return InternalPluginBridge.handle().permissionHooked();
    }
}
