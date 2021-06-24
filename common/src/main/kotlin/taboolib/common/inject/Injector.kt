package taboolib.common.inject

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * TabooLib
 * taboolib.common.inject.Injector
 *
 * @author sky
 * @since 2021/6/24 3:36 下午
 */
class Injector {

    interface Fields : InjectorOrder {

        fun inject(field: Field, clazz: Class<*>, instance: Any?)
    }

    interface Methods : InjectorOrder {

        fun inject(method: Method, clazz: Class<*>, instance: Any?)
    }

    interface Classes : InjectorOrder {

        fun inject(clazz: Class<*>, instance: Any?)
    }
}