package taboolib.module.nms

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import taboolib.common.TabooLib
import taboolib.common.io.taboolibPath
import taboolib.module.nms.remap.RemapTranslation
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
            error("Cannot find class: $source")
        }
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        // 若转译对象为 TabooLib 类，且当前运行环境为 Paper 时，使用内部专用转译器
        val remapper = if (MinecraftVersion.isUniversalCraftBukkit && source.startsWith(taboolibPath)) RemapTranslationTabooLib() else RemapTranslation()
        classReader.accept(ClassRemapper(classWriter, remapper), 0)
        return AsmClassLoader.createNewClass(source, classWriter.toByteArray())
    }
}