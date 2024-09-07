package taboolib.module.nms.test

import net.minecraft.SystemUtils
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.Test
import taboolib.common.Test.Result
import taboolib.common.reflect.ClassHelper
import taboolib.module.nms.*
import java.net.URISyntaxException

/**
 * TabooLib
 * taboolib.module.nms.test.TestNMS
 *
 * @author 坏黑
 * @since 2024/7/21 17:27
 */
object TestNMS : Test() {

    override fun check(): List<Result> {
        val result = arrayListOf<Result>()
        result += sandbox("NMS:isBukkitServerRunning") { isBukkitServerRunning }
        result += sandbox("NMS:minecraftServerObject") { minecraftServerObject }
        // 获取 OBC 类
        result += sandbox("NMS:obcClass") { obcClass("CraftServer") }
        // 获取 NMS 类
        result += sandbox("NMS:nmsClass") { nmsClass("MinecraftServer") }
        // 测试 ClassUtils
        result += sandbox("NMS:ClassUtils") { ClassHelper.getClass("net.minecraft.SystemUtils", false) }
        // 测试动态转译
        result += sandbox("NMS:Translation") { nmsProxy<TestNMSTranslation>().test(result) }
        // 测试非转译环境下的 Reflex
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_17)) {
            result += sandbox("NMS:Reflex(f)") { nmsClass("SystemUtils").getProperty<Any>("a", isStatic = true) }
            result += sandbox("NMS:Reflex(m)") { nmsClass("SystemUtils").invokeMethod<Any>("a", isStatic = true) }
            result += sandbox("NMS:Reflex(m)") {
                try {
                    nmsClass("SystemUtils").invokeMethod<Any>("a", "a", isStatic = true)
                } catch (_: URISyntaxException) {
                }
            }
        }
        return result
    }
}

abstract class TestNMSTranslation {

    abstract fun test(result: MutableList<Result>)
}

class TestNMSTranslationImpl : TestNMSTranslation() {

    override fun test(result: MutableList<Result>) {
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_17)) {
            // 测试转译
            result += Test.sandbox("NMS:Translation:PaperRemap") { SystemUtils.ioPool() }
            // 测试转译环境下的 Reflex
            result += Test.sandbox("NMS:Translation:Reflex(f)") { net.minecraft.SystemUtils::class.java.getProperty<Any>("a", isStatic = true) }
            result += Test.sandbox("NMS:Translation:Reflex(m)") { net.minecraft.SystemUtils::class.java.invokeMethod<Any>("a", isStatic = true) }
            result += Test.sandbox("NMS:Translation:Reflex(m)") {
                try {
                    net.minecraft.SystemUtils::class.java.invokeMethod<Any>("a", "a", isStatic = true)
                } catch (_: URISyntaxException) {
                }
            }
        }
    }
}