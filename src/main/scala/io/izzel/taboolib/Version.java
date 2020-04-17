package io.izzel.taboolib;

import org.bukkit.Bukkit;

import java.util.Arrays;

/**
 * @Author 坏黑
 * @Since 2019-07-05 14:42
 */
public enum Version {

    v1_7(10700), v1_8(10800), v1_9(10900), v1_10(11000), v1_11(11100), v1_12(11200), v1_13(11300), v1_14(11400), v1_15(11500), v1_16(11600), vNull(0);

    private final int versionInt;

    Version(int versionInt) {
        this.versionInt = versionInt;
    }

    public int getVersionInt() {
        return versionInt;
    }

    public static String getBukkitVersion() {
        return Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    public static boolean isAfter(Version in) {
        return getCurrentVersion().getVersionInt() >= in.getVersionInt();
    }

    public static boolean isBefore(Version in) {
        return getCurrentVersion().getVersionInt() < in.getVersionInt();
    }

    public static int getCurrentVersionInt() {
        return getCurrentVersion().versionInt;
    }

    public static Version getCurrentVersion() {
        String nmsVersion = getBukkitVersion();
        return Arrays.stream(values()).filter(value -> nmsVersion.startsWith(value.name())).findFirst().orElse(vNull);
    }
}