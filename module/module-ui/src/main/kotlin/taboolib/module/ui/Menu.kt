package taboolib.module.ui

import org.bukkit.inventory.Inventory
import taboolib.module.ui.type.*
import taboolib.module.ui.type.impl.*
import java.util.concurrent.ConcurrentHashMap

interface Menu {

    /** 标题 */
    var title: String

    /** 构建菜单 */
    fun build(): Inventory

    companion object {

        private val impl = ConcurrentHashMap<Class<*>, Class<*>>()

        init {
            impl[Anvil::class.java] = AnvilImpl::class.java
            impl[Basic::class.java] = BasicImpl::class.java
            impl[Hopper::class.java] = HopperImpl::class.java
            impl[Linked::class.java] = LinkedImpl::class.java
            impl[Stored::class.java] = StoredImpl::class.java
        }

        /** 注册实现 */
        fun registerImplementation(clazz: Class<*>, implementation: Class<*>) {
            impl[clazz] = implementation
        }

        /** 获取实现 */
        fun getImplementation(clazz: Class<*>): Class<*> {
            return impl[clazz] ?: error("Cannot find implementation for ${clazz.name}")
        }
    }
}