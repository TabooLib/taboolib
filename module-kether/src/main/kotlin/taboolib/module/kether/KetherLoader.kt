package taboolib.module.kether

import io.izzel.kether.common.api.QuestActionParser
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import java.lang.reflect.Method

/**
 * TabooLibKotlin
 * taboolib.module.kether.KetherLoader
 *
 * @author sky
 * @since 2021/2/6 3:33 下午
 */
@Awake
object KetherLoader : Injector.Methods {

    override fun inject(method: Method, clazz: Class<*>, instance: Any?) {
        if (method.isAnnotationPresent(KetherParser::class.java)) {
            val parser = method.getAnnotation(KetherParser::class.java)
            if (parser.value.isEmpty()) {
                method.invoke(instance)
            } else {
                parser.value.forEach { name ->
                    Kether.addAction(name, method.invoke(instance) as QuestActionParser, parser.namespace)
                }
            }
        }
    }

    override val priority: Byte
        get() = 0
}