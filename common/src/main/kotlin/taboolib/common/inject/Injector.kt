package taboolib.common.inject

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import taboolib.common.io.LazyInstGetter
import taboolib.common.LifeCycle

abstract class Injector(val lifeCycle: LifeCycle, val priority: Byte = 0) {

    open fun inject(clazz: Class<*>, field: ClassField, instance: LazyInstGetter<*>) {
    }

    open fun inject(clazz: Class<*>, method: ClassMethod, instance: LazyInstGetter<*>) {
    }

    open fun preInject(clazz: Class<*>, instance: LazyInstGetter<*>) {
    }

    open fun postInject(clazz: Class<*>, instance: LazyInstGetter<*>) {
    }
}