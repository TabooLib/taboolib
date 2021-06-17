package taboolib.module.kether

import io.izzel.kether.common.api.QuestActionParser
import taboolib.common.io.classes
import taboolib.common.platform.Awake

/**
 * TabooLibKotlin
 * taboolib.module.kether.KetherLoader
 *
 * @author sky
 * @since 2021/2/6 3:33 下午
 */
@Awake
@Suppress("NO_REFLECTION_IN_CLASS_PATH")
object KetherLoader {

    init {
        classes.forEach base@{ clazz ->
            val instance = clazz.kotlin.objectInstance ?: return@base
            clazz.declaredMethods.forEach {
                if (it.isAnnotationPresent(KetherParser::class.java)) {
                    val parser = it.getAnnotation(KetherParser::class.java)
                    if (parser.value.isEmpty()) {
                        it.invoke(instance)
                    } else {
                        parser.value.forEach { name ->
                            Kether.addAction(name, it.invoke(instance) as QuestActionParser, parser.namespace)
                        }
                    }
                }
            }
        }
    }
}