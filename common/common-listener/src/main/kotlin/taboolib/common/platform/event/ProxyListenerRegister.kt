package taboolib.common.platform.event

import org.tabooproject.reflex.ClassAnnotation
import org.tabooproject.reflex.ClassMethod
import taboolib.common.boot.SimpleServiceLoader
import taboolib.common.io.InstGetter

/**
 * @author 坏黑
 * @since 2022/1/31 6:32 PM
 */
interface ProxyListenerRegister {

    fun register(annotation: ClassAnnotation, method: ClassMethod, instance: InstGetter<*>)

    companion object {

        @JvmField
        val INSTANCE: ProxyListenerRegister = SimpleServiceLoader.load(ProxyListenerRegister::class.java)
    }
}