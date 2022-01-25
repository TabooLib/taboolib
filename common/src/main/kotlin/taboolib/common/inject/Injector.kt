package taboolib.common.inject

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import taboolib.common.InstGetter
import taboolib.common.LifeCycle

/**
 * TabooLib
 * taboolib.common.inject.Injector
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
abstract class Injector(val lifeCycle: LifeCycle, val priority: Byte = 0) {

    open fun inject(clazz: Class<*>, field: ClassField, instance: InstGetter<*>) {
    }

    open fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
    }

    open fun preInject(clazz: Class<*>, instance: InstGetter<*>) {
    }

    open fun postInject(clazz: Class<*>, instance: InstGetter<*>) {
    }
}