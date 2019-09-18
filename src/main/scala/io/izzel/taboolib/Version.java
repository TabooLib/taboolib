package io.izzel.taboolib;

import org.bukkit.Bukkit;

/**
 * @Author 坏黑
 * @Since 2019-07-05 14:42
 */
public enum Version {

    v1_7(10700), v1_8(10800), v1_9(10900), v1_10(11000), v1_11(11100), v1_12(11200), v1_13(11300), v1_14(11400), v1_15(11500), vNull(0);

    private int versionInt;

    Version(int versionInt) {
        this.versionInt = versionInt;
    }

    public int getVersionInt() {
        return versionInt;
    }

    public static boolean isAfter(Version in) {
        return getCurrentVersion().getVersionInt() >= in.getVersionInt();
    }

    public static boolean isBefore(Version in) {
        return getCurrentVersion().getVersionInt() < in.getVersionInt();
    }

    public static String getBukkitVersion() {
        return Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    public static Version getCurrentVersion() {
        String nmsVersion = getBukkitVersion();
        if (nmsVersion.startsWith("v1_7")) {
            return v1_7;
        } else if (nmsVersion.startsWith("v1_8")) {
            return v1_8;
        } else if (nmsVersion.startsWith("v1_9")) {
            return v1_9;
        } else if (nmsVersion.startsWith("v1_10")) {
            return v1_10;
        } else if (nmsVersion.startsWith("v1_11")) {
            return v1_11;
        } else if (nmsVersion.startsWith("v1_12")) {
            return v1_12;
        } else if (nmsVersion.startsWith("v1_13")) {
            return v1_13;
        } else if (nmsVersion.startsWith("v1_14")) {
            return v1_14;
        } else if (nmsVersion.startsWith("v1_15")) {
            return v1_15;
        } else {
            return vNull;
        }
    }
}