package taboolib.module.nms

import org.bukkit.Bukkit
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.reflect.Reflex
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.reflexInvoke
import taboolib.module.dependency.RuntimeDependencies
import taboolib.module.dependency.RuntimeDependency
import taboolib.module.dependency.RuntimeName
import taboolib.module.dependency.RuntimeTest

@RuntimeDependencies(
    RuntimeDependency(group = "org.ow2.asm", id = "asm", version = "9.1", hash = "a99500cf6eea30535eeac6be73899d048f8d12a8"),
    RuntimeDependency(group = "org.ow2.asm", id = "asm-util", version = "9.1", hash = "36464a45d871779f3383a8a9aba2b26562a86729"),
    RuntimeDependency(group = "org.ow2.asm", id = "asm-commons", version = "9.1", hash = "8b971b182eb5cf100b9e8d4119152d83e00e0fdd")
)
@RuntimeName(group = "org.ow2.asm", name = "asm (9.1)")
@RuntimeTest(group = "org.ow2.asm", path = ["org.objectweb.asm.ClassVisitor"])
@Awake
@PlatformSide([Platform.BUKKIT])
object MinecraftVersion {

    val runningVersion by lazy {
        Bukkit.getServer().reflex<Any>("console")!!.reflexInvoke<String>("getVersion")!!
    }

    val supportedVersion = arrayOf(
        arrayOf("1.8", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9"),
        arrayOf("1.9", "1.9.2", "1.9.4"),
        arrayOf("1.10.2"),
        arrayOf("1.11", "1.11.2"),
        arrayOf("1.12", "1.12.1", "1.12.2"),
        arrayOf("1.13", "1.13.1", "1.13.2"),
        arrayOf("1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4"),
        arrayOf("1.15", "1.15.1", "1.15.2"),
        arrayOf("1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5"),
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