package io.izzel.taboolib;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Bukkit 版本检测工具
 * <p>
 * 支持版本为 1.7 到 1.16
 * 命名方式为 {主版本}{次版本}{00}
 * 如 1.7.10 则为 {01}{07}{10} -- 01 07 00 -- 10700
 * 如 1.16.3 则为 {01}{16}{03} -- 01 16 00 -- 11600
 * <p>
 * 目的时使用整数计算进行快速的版本判断
 *
 * @author 坏黑
 * @since 2019-07-05 14:42
 */
public enum Version {

    v1_7(10700),
    v1_8(10800),
    v1_9(10900),
    v1_10(11000),
    v1_11(11100),
    v1_12(11200),
    v1_13(11300),
    v1_14(11400),
    v1_15(11500),
    v1_16_R3(11604),
    v1_16(11600),
    v1_17(11700),
    vNull(0);

    private static Version versionCurrent;
    private final int versionInt;

    Version(int versionInt) {
        this.versionInt = versionInt;
    }

    /**
     * @return 版本数字号
     */
    public int getVersionInt() {
        return versionInt;
    }

    /**
     * @return Bukkit 版本（例如：v1_16_R1）
     */
    public static String getBukkitVersion() {
        return Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    /**
     * 检测服务端是否<b>高于或等于</b>该版本
     *
     * @param in 版本枚举
     * @return boolean
     */
    public static boolean isAfter(Version in) {
        return getCurrentVersion().getVersionInt() >= in.getVersionInt();
    }

    /**
     * 检测服务端是否<b>低于</b>该版本
     *
     * @param in 版本枚举
     * @return boolean
     */
    public static boolean isBefore(Version in) {
        return getCurrentVersion().getVersionInt() < in.getVersionInt();
    }

    /**
     * @return 当前服务端版本数字号
     */
    public static int getCurrentVersionInt() {
        return getCurrentVersion().versionInt;
    }

    /**
     * @return 当前服务端版本枚举
     */
    @NotNull
    public static Version getCurrentVersion() {
        if (versionCurrent == null) {
            versionCurrent = Arrays.stream(values()).filter(value -> getBukkitVersion().startsWith(value.name())).findFirst().orElse(vNull);
        }
        return versionCurrent;
    }
}