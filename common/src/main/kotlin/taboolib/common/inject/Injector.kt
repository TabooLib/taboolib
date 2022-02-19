package taboolib.common.inject

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Supplier

/**
 * TabooLib
 * taboolib.common.inject.Injector
 *
 * @author sky
 * @since 2021/6/24 3:36 下午
 */
class Injector {

    interface Classes : InjectorOrder {

        fun inject(clazz: Class<*>, instance: Supplier<*>)

        fun postInject(clazz: Class<*>, instance: Supplier<*>)
    }

    interface Fields : InjectorOrder {

        fun inject(field: Field, clazz: Class<*>, instance: Supplier<*>)
    }

    interface Methods : InjectorOrder {

        fun inject(method: Method, clazz: Class<*>, instance: Supplier<*>)
    }
}