package taboolib.module.nms

import org.bukkit.Bukkit
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.env.RuntimeResource
import taboolib.common.env.RuntimeResources
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.reflect.Reflex
import java.io.FileInputStream

@RuntimeDependencies(
    RuntimeDependency("org.ow2.asm:asm:9.1", test = "org.objectweb.asm.ClassVisitor"),
    RuntimeDependency("org.ow2.asm:asm-util:9.1", test = "org.objectweb.asm.util.Printer"),
    RuntimeDependency("org.ow2.asm:asm-commons:9.1", test = "org.objectweb.asm.commons.Remapper"),
)
@RuntimeResources(
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-e3c5450d-fields.csrg",
        hash = "e3b7c0dfbce9544ed650230e208865b8c5dea94e"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-00fabbe5-fields.csrg",
        hash = "6e515ad1b4cd49e93e26380e4deca8b876a517a7"
    )
)
@PlatformSide([Platform.BUKKIT])
object MinecraftVersion {

    val runningVersion by lazy {
        val version = Bukkit.getServer().version.split("MC:")[1]
        version.substring(0, version.length - 1).trim()
    }

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
        arrayOf("1.17", "1.17.1")
    )

    val major by lazy {
        supportedVersion.indexOfFirst { it.contains(runningVersion) }
    }

    val isSupported by lazy {
        supportedVersion.flatten().contains(runningVersion)
    }

    val isUniversal by lazy {
        major >= 9
    }

    val mappingFields = mapOf("1.17" to "e3c5450d", "1.17.1" to "00fabbe5")

    val mapping by lazy {
        if (isUniversal && mappingFields.containsKey(runningVersion)) {
            Mapping(FileInputStream("assets/bukkit-${mappingFields[runningVersion]!!}-fields.csrg"))
        } else {
            null
        }
    }

    val legacyVersion by lazy {
        Bukkit.getServer().javaClass.name.split('.')[3]
    }

    init {
        Reflex.remapper.add(RefRemapper)
    }
}