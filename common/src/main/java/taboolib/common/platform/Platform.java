package taboolib.common.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * TabooLib
 * taboolib.common.platform.Platform
 *
 * @author sky
 * @since 2021/6/16 1:41 上午
 */
public enum Platform {

    BUKKIT("Bukkit", "org.bukkit.Bukkit"),

    BUNGEE("Bungee", "net.md_5.bungee.BungeeCord"),

    VELOCITY("Velocity", "com.velocitypowered.api.plugin.Plugin"),

    APPLICATION("Application", null);

    /**
     * 当前运行平台
     */
    public static final Platform CURRENT = current();

    @NotNull
    final String key;
    @Nullable
    final String checkClass;

    Platform(@NotNull String key, @Nullable String checkClass) {
        this.key = key;
        this.checkClass = checkClass;
    }

    @NotNull
    public String key() {
        return key;
    }

    @Nullable
    public String checkClass() {
        return checkClass;
    }

    /**
     * 获取属于 Minecraft 的平台类型
     */
    public static Platform[] minecraft() {
        return new Platform[]{BUKKIT, BUNGEE, VELOCITY};
    }

    /**
     * 获取当前运行平台
     */
    private static Platform current() {
        for (Platform platform : Platform.minecraft()) {
            try {
                Class.forName(platform.checkClass());
                return platform;
            } catch (ClassNotFoundException ignored) {
            }
        }
        return Platform.APPLICATION;
    }
}