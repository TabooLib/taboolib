package taboolib.module.nms

import org.bukkit.Bukkit
import org.tabooproject.reflex.Reflex
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.UnsupportedVersionException
import taboolib.common.io.isDevelopmentMode
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.runningPlatform
import taboolib.common.platform.function.warning
import taboolib.common.util.t
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.remap.RemapReflexPaper
import taboolib.module.nms.remap.RemapReflexSpigot
import taboolib.platform.bukkit.Exchanges
import java.io.FileInputStream

@Inject
@PlatformSide(Platform.BUKKIT)
object MinecraftVersion {

    const val V1_8 = 0
    const val V1_9 = 1
    const val V1_10 = 2
    const val V1_11 = 3
    const val V1_12 = 4
    const val V1_13 = 5
    const val V1_14 = 6
    const val V1_15 = 7
    const val V1_16 = 8
    const val V1_17 = 9
    const val V1_18 = 10
    const val V1_19 = 11
    const val V1_20 = 12
    const val V1_21 = 13

    /**
     * 当前运行的版本（字符版本），例如：v1_8_R3
     * 在 Paper 1.20.6+ 此方法失效，返回 "UNKNOWN"
     */
    val minecraftVersion by unsafeLazy {
        val version = Bukkit.getServer().javaClass.name.split('.')[3]
        if (version.startsWith('v')) version else "UNKNOWN"
    }

    /**
     * 当前运行的版本（数字版本），例如：1.8.8
     */
    val runningVersion by unsafeLazy {
        val version = Bukkit.getServer().version.split("MC:")[1]
        version.substring(0, version.length - 1).trim()
    }

    /**
     * 是否为 universal obc 版本（一般表现为 Paper 1.10.6+ 环境）
     */
    val isUniversalCraftBukkit: Boolean
        get() = minecraftVersion == "UNKNOWN"

    /**
     * 当前所有受支持的版本
     */
    val supportedVersion = arrayOf(
        // @formatter:off
        arrayOf("1.8", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9"),   // 0
        arrayOf("1.9", "1.9.2", "1.9.4"),                                                // 1
        arrayOf("1.10.2"),                                                               // 2
        arrayOf("1.11", "1.11.2"),                                                       // 3
        arrayOf("1.12", "1.12.1", "1.12.2"),                                             // 4
        arrayOf("1.13", "1.13.1", "1.13.2"),                                             // 5
        arrayOf("1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4"),                         // 6
        arrayOf("1.15", "1.15.1", "1.15.2"),                                             // 7
        arrayOf("1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5"),                       // 8
        arrayOf("1.17", "1.17.1"),                                                       // 9 (universal)
        arrayOf("1.18", "1.18.1", "1.18.2"),                                             // 10
        arrayOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4"),                         // 11
        arrayOf("1.20", "1.20.1", "1.20.2", "!1.20.3", "1.20.4", "!1.20.5", "1.20.6"),   // 12 (跳过 1.20.3、1.20.5) NOTICE 从 1.20.5 开始, paper 进行了破坏性修改
        arrayOf("!1.21", "1.21.1", "!1.21.2", "1.21.3")                                  // 13 (跳过 1.21 和 1.21.2)
        // @formatter:on
    )

    /**
     * 版本 ID，使用 TabooLib 格式
     * 例如：
     * + 1.8.8  -> 1 08 08 -> 10808
     * + 1.12.2 -> 1 12 02 -> 11202
     * + 1.21.1 -> 1 21 01 -> 12101
     */
    val versionId by unsafeLazy {
        when (major) {
            V1_8 -> 10800
            V1_9 -> 10900
            V1_10 -> 11000
            V1_11 -> 11100
            V1_12 -> 11200
            V1_13 -> 11300
            V1_14 -> 11400
            V1_15 -> 11500
            V1_16 -> 11600
            V1_17 -> 11700
            V1_18 -> 11800
            V1_19 -> 11900
            V1_20 -> 12000
            V1_21 -> 12100
            else -> 0
        } + minor
    }

    @Deprecated("Use versionId instead.", ReplaceWith("versionId"))
    val majorLegacy: Int
        get() = versionId

    /**
     * 主版本号
     */
    val major by unsafeLazy {
        supportedVersion.indexOfFirst { it.contains(runningVersion) }
    }

    /**
     * 次版本号
     */
    val minor by unsafeLazy {
        if (major != -1) {
            supportedVersion[major].indexOf(runningVersion)
        } else {
            -1
        }
    }

    /**
     * 是否支持当前运行版本
     */
    val isSupported by unsafeLazy {
        supportedVersion.flatten().contains(runningVersion)
    }

    /**
     * 是否运行在一个被跳过的版本
     */
    val isSkipped by unsafeLazy {
        supportedVersion.flatten().any { it.startsWith("!") && runningVersion == it.substring(1) }
    }

    /**
     * 是否为 1.17 以上版本
     */
    val isUniversal by unsafeLazy {
        major >= V1_17
    }

    /**
     * 是否支持 BundlePacket 数据包（1.19.4+）
     */
    val isBundlePacketSupported by unsafeLazy {
        majorLegacy >= 11904
    }

    /**
     * 当前运行版本的 Spigot 映射文件
     */
    val spigotMapping by unsafeLazy {
        // 如果已被其他插件加载，直接从内存中读取
        if (Exchanges.MAPPING_SPIGOT in Exchanges) {
            Mapping.exchange(Exchanges.MAPPING_SPIGOT)
        } else {
            val current = SpigotMapping.current
            if (current == null) {
                disablePlugin()
                throw UnsupportedVersionException()
            }
            Mapping.spigot(
                FileInputStream("assets/${current.combined.substring(0, 2)}/${current.combined}"),
                FileInputStream("assets/${current.fields.substring(0, 2)}/${current.fields}"),
            ).exchange(Exchanges.MAPPING_SPIGOT)
        }
    }

    /**
     * 当前运行版本的 Paper 映射文件
     * 仅用与对 TabooLib 本体的 NMSProxy Impl 进行二次转译（插件本体会自动转译）
     *
     * ```
     * 方法/字段逻辑：
     * Spigot Deobf -> Mojang Obf -> Mojang Deobf
     * ^
     * 还原为 Mojang Obf，逆向查找 Mojang Deobf，因为 Paper 环境采用的是 Mojang Deobf
     * 以 SystemUtils 为例：
     * net/minecraft/SystemUtils.ioPool() -> net/minecraft/SystemUtils.g() -> net/minecraft/Util.backgroundExecutor()
     *                                                                     ^
     *                                                      此时进入 reobf.tiny 文件检索
     * 类逻辑：
     * Spigot Deobf -> Mojang Deobf
     * ^
     * 根据 Paper 提供的 reobf.tiny 直接从 Spigot Deobf 转换为 Mojang Deobf
     * 以 SystemUtils 为例：
     * net/minecraft/SystemUtils -> net/minecraft/Util
     * ```
     *
     * 这么做的原因是要保证 TabooLib 本体必须能够在 Spigot 环境下运行。
     */
    val paperMapping by unsafeLazy {
        // 如果已被其他插件加载，直接从内存中读取
        if (Exchanges.MAPPING_PAPER in Exchanges) {
            Mapping.exchange(Exchanges.MAPPING_PAPER)
        } else {
            Mapping.paper().exchange(Exchanges.MAPPING_PAPER)
        }
    }

    /**
     * 是否高于某个版本，使用方式如下：
     * ```
     * MinecraftVersion.isHigher(MinecraftVersion.V1_12)
     * ```
     */
    fun isHigher(version: Int): Boolean {
        return version < major
    }

    /**
     * 是否高于或等于某个版本
     */
    fun isHigherOrEqual(version: Int): Boolean {
        return version <= major
    }

    /**
     * 是否低于某个版本
     */
    fun isLower(version: Int): Boolean {
        return version > major
    }

    /**
     * 是否低于或等于某个版本
     */
    fun isLowerOrEqual(version: Int): Boolean {
        return version >= major
    }

    /**
     * 是否在某个版本范围内
     */
    fun isIn(range: IntRange): Boolean {
        return major in range
    }

    /**
     * 是否在某个版本范围内
     */
    fun isIn(min: Int, max: Int): Boolean {
        return major in min..max
    }

    /**
     * 是否等于某个版本
     */
    fun isEqual(version: Int): Boolean {
        return version == major
    }

    @Awake(LifeCycle.LOAD)
    private fun init() {
        if (!isSupported) {
            // 是否运行在一个被跳过的版本，将予以特殊的提示信息
            if (isSkipped) {
                warning(
                    """
                    当前 Minecraft 版本已跳过支持，通常由于 Mojang 官方发布了紧急修复版本。
                    The current Minecraft version has been skipped for support, usually due to an emergency fix released by Mojang.
                    """.t()
                )
            } else {
                warning(
                    """
                    当前 Minecraft 版本不受支持，请等待插件适配。
                    The current Minecraft version is not supported, please wait for the plugin to be adapted.
                    """.t()
                )
            }
            // 仅在非开发模式下禁用插件
            if (!isDevelopmentMode) disablePlugin()
        }
        // 在 Bukkit 平台下，注册 Reflex 重定向实现
        if (runningPlatform == Platform.BUKKIT) {
            Reflex.remapper.add(if (isUniversalCraftBukkit) RemapReflexPaper() else RemapReflexSpigot())
        }
    }
}