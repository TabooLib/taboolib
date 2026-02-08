package taboolib.module.nms

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import taboolib.common.TabooLib
import taboolib.common.BinaryCache
import taboolib.common.io.digest
import taboolib.common.io.taboolibPath
import taboolib.common.platform.function.debug
import taboolib.common.util.execution
import taboolib.common.util.t
import taboolib.module.nms.remap.RemapTranslation
import taboolib.module.nms.remap.RemapTranslationLegacy
import taboolib.module.nms.remap.RemapTranslationTabooLib

/**
 * TabooLib 所使用的 "org.objectweb.asm" 是经过重定向后的，通常表现为 "org.objectweb.asm9"。
 *
 * 简单来说：
 * 1. 插件本体会被 Paper 自动转译
 * 2. TabooLib 作为外置依赖，无法通过 Paper 的自动转译
 *
 * 因此需要使用 Paper 的内部工具 ReflectionRemapper 来转译 TabooLib 本体（NMSProxy Impl）
 * 但是这是逻辑冲突的，TabooLib 需要对 ASM 重定向以保证兼容性，而 ReflectionRemapper 只能接受原生的 ASM 类
 *
 * 可能最终只能使用一种丑陋的解决办法：
 * 在 "common" 模块中对 ASM 进行检测和版本判定，若存在 "Opcodes.ASM9" 则不再加载和重定向 ASM 库。
 *
 * NOTICE 2024/7/21 04:05
 * 经测试，ReflectionRemapper 无效，原因不详。
 *
 * @author sky
 * @since 2021/6/18 1:49 上午
 */
class AsmClassTranslation(val source: String) {

    @Synchronized
    fun createNewClass(): Class<*> {
        var inputStream = AsmClassTranslation::class.java.classLoader.getResourceAsStream(source.replace('.', '/') + ".class")
        if (inputStream == null) {
            inputStream = TabooLib::class.java.classLoader.getResourceAsStream(source.replace('.', '/') + ".class")
        }
        if (inputStream == null) {
            error(
                """
                    没有找到将被转译的类 $source
                    No class found to be translated $source
                """.t()
            )
        }
        val bytes = inputStream.readBytes()
        val srcVersion = bytes.digest()
        // 若存在缓存则直接读取
        val (cacheClass, cost) = execution { BinaryCache.read("remap/$source", srcVersion) { AsmClassLoader.createNewClass(source, it) } }
        if (cacheClass != null) {
            debug("[AsmClassTranslation] 从缓存中加载 $source，用时 $cost 毫秒。")
            return cacheClass
        }
        // 转译
        val (newClass, cost2) = execution {
            val classReader = ClassReader(bytes)
            val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
            // 若当前运行环境为 Paper 时使用新版转换器
            val remapper = if (MinecraftVersion.isUniversalCraftBukkit) {
                // 若转译对象为 TabooLib 类，需要特殊处理
                if (source.startsWith(taboolibPath)) RemapTranslationTabooLib() else RemapTranslation()
            }
            // 使用旧版本转译器
            else {
                RemapTranslationLegacy()
            }
            classReader.accept(ClassRemapper(classWriter, remapper), 0)
            val newBytes = classWriter.toByteArray()
            // 缓存
            BinaryCache.save("remap/$source", srcVersion) { newBytes }
            AsmClassLoader.createNewClass(source, newBytes)
        }
        debug("[AsmClassTranslation] 转译 $source，用时 $cost2 毫秒。")
        return newClass
    }
}