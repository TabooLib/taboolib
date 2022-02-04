package taboolib.module.nms

import org.bukkit.Bukkit
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import org.tabooproject.reflex.Reflex
import taboolib.common.TabooLib
import java.io.FileInputStream

@PlatformSide([Platform.BUKKIT])
object MinecraftVersion {

    val minecraftVersion by lazy {
        Bukkit.getServer().javaClass.name.split('.')[3]
    }

    /**
     * 当前正在运行的版本
     */
    val runningVersion by lazy {
        val version = Bukkit.getServer().version.split("MC:")[1]
        version.substring(0, version.length - 1).trim()
    }

    /**
     * 当前所有受支持的版本
     */
    val supportedVersion = arrayOf(
        arrayOf("1.8", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9"), // 0
        arrayOf("1.9", "1.9.2", "1.9.4"), // 1
        arrayOf("1.10.2"), // 2
        arrayOf("1.11", "1.11.2"), // 3
        arrayOf("1.12", "1.12.1", "1.12.2"), // 4
        arrayOf("1.13", "1.13.1", "1.13.2"), // 5
        arrayOf("1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4"), // 6
        arrayOf("1.15", "1.15.1", "1.15.2"), // 7
        arrayOf("1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5"), // 8
        // universal >= 9
        arrayOf("1.17", "1.17.1"),
        arrayOf("1.18", "1.18.1"), // 10
    )

    /**
     * 老版本格式
     */
    val majorLegacy by lazy {
        when (major) {
            0 -> 10800
            1 -> 10900
            2 -> 11000
            3 -> 11100
            4 -> 11200
            5 -> 11300
            6 -> 11400
            7 -> 11500
            8 -> 11600
            9 -> 11700
            10 -> 11800
            else -> 0
        } + minor
    }

    /**
     * 主版本号
     */
    val major by lazy {
        supportedVersion.indexOfFirst { it.contains(runningVersion) }
    }

    /**
     * 次版本号
     */
    val minor by lazy {
        if (major != -1) {
            supportedVersion[major].indexOf(runningVersion)
        } else {
            -1
        }
    }

    /**
     * 是否支持当前运行版本
     */
    val isSupported by lazy {
        supportedVersion.flatten().contains(runningVersion)
    }

    /**
     * 是否为 1.17 以上版本
     */
    val isUniversal by lazy {
        major >= 9
    }

    val mapping by lazy {
        val mappingFile = if (isUniversal) MappingFile.files[runningVersion]!! else MappingFile.files["1.17"]!!
        Mapping(
            FileInputStream("assets/${mappingFile.combined.substring(0, 2)}/${mappingFile.combined}"),
            FileInputStream("assets/${mappingFile.fields.substring(0, 2)}/${mappingFile.fields}"),
        )
    }

    init {
        if (TabooLib.runningPlatform() == Platform.BUKKIT) {
            Reflex.remapper.add(RefRemapper)
        }
    }
}