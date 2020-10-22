package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import org.bukkit.OfflinePlayer;

/**
 * Vault 支持
 *
 * @Author 坏黑
 * @Since 2019-07-05 18:50
 */
public class EconomyHook {

    /**
     * 赋予货币
     *
     * @param p 玩家
     * @param d 数据
     */
    public static void add(OfflinePlayer p, double d) {
        InternalPluginBridge.handle().economyGive(p, d);
    }

    /**
     * 移除货币
     *
     * @param p 玩家
     * @param d 数据
     */
    public static void remove(OfflinePlayer p, double d) {
        InternalPluginBridge.handle().economyTake(p, d);
    }

    /**
     * 设置货币
     *
     * @param p 玩家
     * @param d 数据
     */
    public static void set(OfflinePlayer p, double d) {
        add(p, d - get(p));
    }

    /**
     * 获取货币
     *
     * @param p 玩家
     */
    public static double get(OfflinePlayer p) {
        return InternalPluginBridge.handle().economyLook(p);
    }

    /**
     * 创建数据
     *
     * @param p 玩家
     */
    public static void create(OfflinePlayer p) {
        InternalPluginBridge.handle().economyCreate(p);
    }

    /**
     * 是否支持
     */
    public static boolean exists() {
        return InternalPluginBridge.handle().economyHooked();
    }
}
