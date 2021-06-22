package taboolib.module.nms

import org.bukkit.Bukkit
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.reflect.Reflex
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.reflexInvoke

@RuntimeDependencies(
    RuntimeDependency("org.ow2.asm:asm:9.1", test = "org.objectweb.asm.ClassVisitor"),
    RuntimeDependency("org.ow2.asm:asm-util:9.1", test = "org.objectweb.asm.util.Printer"),
    RuntimeDependency("org.ow2.asm:asm-commons:9.1", test = "org.objectweb.asm.commons.Remapper"),
)
@Awake
@PlatformSide([Platform.BUKKIT])
object MinecraftVersion {

    val runningVersion by lazy {
        Bukkit.getServer().reflex<Any>("console")!!.reflexInvoke<String>("getVersion")!!
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
        arrayOf("1.17")
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

    val mappingFields = mapOf("1.17" to "bukkit-e3c5450d-fields.csrg")

    val mapping by lazy {
        if (isUniversal && mappingFields.containsKey(runningVersion)) {
            Mapping(MinecraftVersion::class.java.getResourceAsStream(mappingFields[runningVersion]!!))
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