package taboolib.module.nms.remap

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.Remapper
import taboolib.common.reflect.ClassHelper
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.remap.RemapTranslation.Companion.extraTransformers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 插件内部的类
 *
 * 对于 TabooLib 内的类，
 * 使用 RemapTranslationTabooLib 进行 Spigot Deobf -> Mojang Obf -> Mojang Deobf 转换。
 *
 * 而插件内的类，已经由 Paper 进行转译了，所以不应该再使用 RemapTranslation (现在为 RemapTranslationPlugin) 进行转译，
 * 应该只需要使用该 RemapTranslation 移除包名中的跨版本信息 (诸如 v1_20_R3) ，
 * 而无需对字段名、方法名进行任何操作。
 *
 * 只有 Paper 1.20.5+ 才会启用该类用于转译插件本体里的类，一般使用其子类 RemapTranslationPlugin。
 * 与 RemapTranslationTabooLib 不同的是，此实现不会对函数名和字段名进行检索转译，避免 Paper 对插件本体的转译失效。
 *
 * @author mical
 * @since 2024/8/17 22:44
 */
open class RemapTranslation : Remapper() {

    val obc1 = "org/bukkit/craftbukkit/v1_.*?/".toRegex()
    val obc2 = "org/bukkit/craftbukkit/${MinecraftVersion.minecraftVersion}/"
    val obc3 = "org/bukkit/craftbukkit/"
    val runtimeClassCache = ConcurrentHashMap<String, Boolean>()

    companion object {

        /**
         * 额外的字节码变换器 —— 在 remap + require + dynamic 三步之后执行。
         *
         * 供 [taboolib.module.incision] 等模块在 NMSProxy 管线末端注入自定义织入逻辑。
         * 每个 transformer 的签名：`(className, bytes) -> ByteArray?`；
         * 返回 null 表示不修改，返回非 null 则替换当前字节码继续传递下一个 transformer。
         *
         * 线程安全：使用 [java.util.concurrent.CopyOnWriteArrayList]。
         */
        @JvmStatic
        val extraTransformers: MutableList<(String, ByteArray) -> ByteArray?> = CopyOnWriteArrayList()
    }

    /** 运行 [extraTransformers] 管线；供 [taboolib.module.nms.AsmClassTranslation] 调用。 */
    fun applyExtraTransforms(className: String, classBytes: ByteArray): ByteArray {
        if (extraTransformers.isEmpty()) return classBytes
        var cur = classBytes
        for (t in extraTransformers) {
            try {
                val out = t(className, cur)
                if (out != null) cur = out
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return cur
    }

    override fun map(internalName: String): String {
        return translate(internalName)
    }

    /**
     * 包名转换方法
     */
    open fun translate(key: String): String {
        // obc
        if (key.startsWith("org/bukkit/craftbukkit")) {
            // 如果是 Universal CraftBukkit，则必须去除版本号
            if (MinecraftVersion.isUniversalCraftBukkit) {
                return key.replace(obc1, obc3)
            }
            // 若非 Universal CraftBukkit 环境，则判断是否有版本号
            // 如果没有版本号，则补上版本号
            if (!key.startsWith("org/bukkit/craftbukkit/v1_")) {
                return key.replace(obc3, obc2)
            }
            // 如果有版本号，则替换版本号为当前正在运行的版本
            return key.replace(obc1, obc2)
        }
        // 统一版本
        return if (MinecraftVersion.isUniversal) {
            // 将低版本包名替换为高版本包名
            // net/minecraft/server/v1_17_R1/EntityPlayer -> net/minecraft/server/level/EntityPlayer
            if (key.startsWith("net/minecraft/server/v1_")) {
                // 先转为 Spigot.FullName
                var spigotName = MinecraftVersion.spigotMapping.classMapSpigotS2F[key.substringAfterLast('/')] ?: return key
                // 如果为 Mojang Mapping 环境, 则应进一步转译为 Mojang.FullName
                spigotName = if (MinecraftVersion.isMojangMapping) MinecraftVersion.paperMapping.classMapSpigotToMojang[spigotName] ?: spigotName else spigotName
                spigotName.replace('.', '/')
            } else {
                // 如果是非 Mojang Mapping 环境，且这里是 Mojang.Fullname，则：尝试获取 Spigot.Fullname 并返回，如果获取不到，那么 key 就是 Spigot.Fullname 本身
                if (!MinecraftVersion.isMojangMapping) {
                    translateMojangToSpigotOrKeepRuntime(key)
                } else {
                    translateMojangToRuntimeOrKeep(key)
                }
            }
        } else {
            // 如果是 Mojang.Fullname 则尝试寻找对应的 Spigot.Fullname
            if (key.startsWith("net/minecraft")) {
                // 旧版 NMS 只有 net.minecraft.server.vX_Y_RZ 包，映射缺失时仍要用短类名回落到当前运行版本。
                val translated = findMojangToSpigotName(key) ?: key
                "net/minecraft/server/${MinecraftVersion.minecraftVersion}/${translated.substringAfterLast('/')}"
            } else key
        }
    }

    /**
     * 只从 Paper 映射中查找 Mojang 类名对应的 Spigot 类名。
     */
    fun findMojangToSpigotName(key: String): String? {
        return MinecraftVersion.paperMapping.classMapMojangToSpigot[key.replace('/', '.')]?.replace('.', '/')
    }

    /**
     * 将 Mojang 类名转为 Spigot 类名，运行时已有类名优先保留。
     */
    fun translateMojangToSpigotOrKeepRuntime(key: String): String {
        val runtimeName = key.replace('/', '.')
        val spigotName = findMojangToSpigotName(key) ?: return key
        // 只有映射表确实准备改名时才检查运行时类，避免在普通路径上反复触发类查找。
        if (spigotName == key) {
            return key
        }
        if (hasRuntimeClass(runtimeName)) {
            return key
        }
        return spigotName
    }

    /**
     * 将 Mojang 类名转为 Runtime 类名，运行时已有类名优先保留。
     */
    fun translateMojangToRuntimeOrKeep(key: String): String {
        val runtimeName = key.replace('/', '.')
        if (hasRuntimeClass(runtimeName)) {
            return key
        }
        val shortName = runtimeName.substringAfterLast('.')
        val mappingName = MinecraftVersion.paperMapping.classMapSpigotToMojang[runtimeName]
            ?: MinecraftVersion.paperMapping.classMapSpigotToMojang.values.singleOrNull { it.substringAfterLast('.') == shortName }
            ?: return key
        return if (hasRuntimeClass(mappingName)) mappingName.replace('.', '/') else key
    }

    /**
     * 检查类名是否已是当前运行时可直接加载的名称。
     *
     * 1.17-1.20.4 存在 Mojang 与 Spigot 同名但语义不同的类，
     * 例如 Mojang 的 MobEffect 是效果类型，而 Spigot 的 MobEffect 是效果实例。
     * 先保留运行时已有类名，避免把已正确的 Spigot 名称再次映射到另一个类。
     */
    fun hasRuntimeClass(name: String): Boolean {
        return runtimeClassCache.getOrPut(name) {
            try {
                Class.forName(name, false, javaClass.classLoader)
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    /**
     * 获取类的所有父类和接口
     * 因为映射信息是以实际所在的类为准，如果不向上追溯，那么调用子类的方法时会找不到映射信息
     */
    fun findParents(owner: String): Set<String> {
        if (owner.startsWith("net.minecraft") || owner.startsWith("com.mojang")) {
            try {
                val find = hashSetOf<String>()
                find += owner
                val forName = ClassHelper.getClass(owner)
                find += forName.interfaces.map { it.name }
                val superclass = forName.superclass
                if (superclass != null && superclass.name != "java.lang.Object") {
                    find += findParents(superclass.name)
                }
                return find
            } catch (_: Throwable) {
            }
        }
        return hashSetOf(owner)
    }

    /**
     * 对字节码应用 dynamic 转换
     * 检测代码中的 dynamic() 调用，将其替换为直接 JVM 指令
     *
     * @param classBytes 原始类字节码
     * @return 转换后的字节码
     */
    fun applyDynamicTransform(classBytes: ByteArray): ByteArray {
        return try {
            val classReader = ClassReader(classBytes)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val classVisitor = DynamicClassVisitor(Opcodes.ASM9, classWriter, this)
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            classWriter.toByteArray()
        } catch (e: Throwable) {
            e.printStackTrace()
            classBytes
        }
    }

    /**
     * 对字节码应用 require 转换
     * 检测代码中的 require(SomeClass::class.java) 调用，
     * 将类名转译后检查其是否存在，然后将整个 require 调用替换为 true 或 false
     *
     * @param classBytes 原始类字节码
     * @return 转换后的字节码
     */
    fun applyRequireTransform(classBytes: ByteArray): ByteArray {
        return try {
            val classReader = ClassReader(classBytes)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val classVisitor = RequireClassVisitor(Opcodes.ASM9, classWriter, this)
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            classWriter.toByteArray()
        } catch (e: Throwable) {
            // 如果转换失败，返回原始字节码
            e.printStackTrace()
            classBytes
        }
    }
}
