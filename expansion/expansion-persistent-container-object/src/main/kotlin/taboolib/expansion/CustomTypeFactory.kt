package taboolib.expansion

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

/**
 * TabooLib
 * taboolib.expansion.CustomTypeFactory
 *
 * @author 坏黑
 * @since 2023/8/12 01:05
 */
@Awake
class CustomTypeFactory : ClassVisitor() {

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.INIT
    }

    override fun visitStart(clazz: Class<*>, instance: Supplier<*>?) {
        if (CustomType::class.java != clazz && CustomType::class.java.isAssignableFrom(clazz)) {
            if (instance == null) {
                error("Cannot create instance of CustomType: $clazz")
            }
            registeredTypes[clazz] = instance.get() as CustomType
        }
    }

    companion object {

        /** 已注册的所有自定义类型 */
        val registeredTypes = ConcurrentHashMap<Class<*>, CustomType>()

        /** 通过值获取自定义类型 */
        fun getCustomType(value: Any): CustomType? {
            return registeredTypes.values.find { it.match(value) }
        }

        /** 通过类获取自定义类型 */
        fun getCustomTypeByClass(clazz: Class<*>): CustomType? {
            return registeredTypes.values.find { it.matchType(clazz) }
        }
    }
}