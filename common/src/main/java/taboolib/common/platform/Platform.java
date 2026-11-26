package taboolib.common.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    AFYBROKER("AfyBroker", "net.afyer.afybroker.server.Broker"),

    HYTALE("Hytale", "com.hypixel.hytale.server.core.plugin.JavaPlugin"),

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
        return new Platform[]{BUKKIT, VELOCITY, BUNGEE, AFYBROKER, HYTALE};
    }

    /**
     * 获取当前运行平台
     */
    private static Platform current() {
        for (Platform platform : Platform.minecraft()) {
            // Velocity API 可能被其他插件 shade (如 Snap), 仅凭 API 类会误判成 Bungee。
            // 要求同时存在 Velocity 服务端实现类, 确保检测的是真正的 Velocity 环境。
            if (platform == VELOCITY) {
                if (isPresent(VELOCITY.checkClass()) && isPresent("com.velocitypowered.proxy.VelocityServer")) {
                    return VELOCITY;
                }
                continue;
            }
            if (isPresent(platform.checkClass())) {
                return platform;
            }
        }
        return Platform.APPLICATION;
    }

    private static boolean isPresent(String className) {
        if (className == null) {
            return false;
        }
        try {
            Class.forName(className);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}