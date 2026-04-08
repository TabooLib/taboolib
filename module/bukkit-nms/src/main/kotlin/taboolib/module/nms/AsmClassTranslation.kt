package taboolib.module.nms

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import taboolib.common.BinaryCache
import taboolib.common.BinaryCache.getCacheFile
import taboolib.common.TabooLib
import taboolib.common.io.digest
import taboolib.common.io.newFile
import taboolib.common.io.taboolibPath
import taboolib.common.platform.function.debug
import taboolib.common.util.execution
import taboolib.common.util.t
import taboolib.module.nms.remap.RemapTranslation
import taboolib.module.nms.remap.RemapTranslationLegacy
import taboolib.module.nms.remap.RemapTranslationTabooLib
import taboolib.module.nms.remap.RemapTranslationUnobfuscated

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
        // 将源代码版本与环境信息组合，生成复合缓存键
        val targetEnvironmentInfo = getTargetEnvironmentInfo()
        val combinedVersion = (srcVersion + targetEnvironmentInfo).digest()
        // 若存在缓存则直接读取
        val (cacheClass, cost) = execution { BinaryCache.read("remap/$source", combinedVersion) { AsmClassLoader.createNewClass(source, it) } }
        if (cacheClass != null) {
            debug("[AsmClassTranslation] 从缓存中加载 $source，用时 $cost 毫秒。")
            return cacheClass
        }
        // 转译
        val (newClass, cost2) = execution {
            val classReader = ClassReader(bytes)
            val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
            // 若当前运行环境为非混淆服务端，则不应该进行除 dynamic、requires 外的任何转译操作
            val remapper = if (MinecraftVersion.isUnobfuscated) {
                RemapTranslationUnobfuscated()
            } else if (MinecraftVersion.isUniversalCraftBukkit) {
                // 若当前运行环境为 Paper 时使用新版转换器
                // 若转译对象为 TabooLib 类，需要特殊处理
                if (source.startsWith(taboolibPath)) RemapTranslationTabooLib() else RemapTranslation()
            }
            // 使用旧版本转译器
            else {
                RemapTranslationLegacy()
            }
            classReader.accept(ClassRemapper(classWriter, remapper), 0)
            var newBytes = classWriter.toByteArray()
            // 应用 require 转换（检测并替换 require 调用）
            newBytes = remapper.applyRequireTransform(newBytes)
            // 应用 dynamic 转换（检测并替换 dynamic() 调用为直接 JVM 指令）
            newBytes = remapper.applyDynamicTransform(newBytes)
            // 缓存
            BinaryCache.save("remap/$source", combinedVersion) { newBytes }
            // 保存字节码用于调试
            newFile(getCacheFile().resolve("binary/remap/${source}.class")).writeBytes(newBytes)
            AsmClassLoader.createNewClass(source, newBytes)
        }
        debug("[AsmClassTranslation] 转译 $source，用时 $cost2 毫秒。")
        return newClass
    }

    /**
     * 获取目标运行环境信息，用于区分不同的服务端环境
     * 当环境发生变化时（如从 Spigot 切换到 Arclight），将强制重新转译
     */
    private fun getTargetEnvironmentInfo(): String {
        val mcRunningVersion = MinecraftVersion.runningVersion
        val mcNmsVersion = MinecraftVersion.minecraftVersion
        val isUniversal = MinecraftVersion.isUniversal
        val isUniversalCB = MinecraftVersion.isUniversalCraftBukkit
        val isMojangMapping = MinecraftVersion.isMojangMapping
        val isUnobfuscated = MinecraftVersion.isUnobfuscated
        return "mcRunning:$mcRunningVersion-nms:$mcNmsVersion-universal:$isUniversal-universalCB:$isUniversalCB-mojangMapping:$isMojangMapping-unobfuscated:$isUnobfuscated"
    }
}