package taboolib.expansion

import org.tabooproject.reflex.ReflexClass
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.expansion.CustomTypeFactory
 *
 * @author 坏黑
 * @since 2023/8/12 01:05
 */
@Inject
@Awake
class CustomTypeFactory : ClassVisitor() {

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.INIT
    }

    override fun visitStart(clazz: ReflexClass) {
        if (clazz.structure.interfaces.any { it.name == CustomType::class.java.name }) {
            registeredTypes[clazz.structure.owner.instance!!] = findInstance(clazz) as? CustomType ?: error("CustomType must have an instance")
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