package taboolib.module.nms.component

import taboolib.common.io.runningClassMapWithoutLibrary
import taboolib.module.nms.AsmClassTranslation

/**
 * ComposedType - 物品组件类型
 * 
 * @author TheFloodDragon
 * @since 2026/2/19 16:17
 */
abstract class ComposedType<T : Any> {

    /**
     * 获取操作层组件
     *
     * @param item NMS物品实例 [net.minecraft.world.item.ItemStack]
     * @return (空代表找不到该组件)
     */
    abstract fun get(item: Any): T?

    /**
     * 设置操作层组件
     *
     * @param item NMS物品实例 [net.minecraft.world.item.ItemStack]
     * @param value 操作层组件数据
     */
    abstract fun set(item: Any, value: T)

    /**
     * 删除该组件
     */
    abstract fun remove(item: Any)

    companion object {

        private val typeProxyInstanceMap = mutableMapOf<String, ComposedType<*>>()

        /**
         * 构造物品组件类型实例
         *
         * @param fullName 目标类的全限定类名
         */
        fun <T : Any> of(fullName: String): ComposedType<T> {
            // 从缓存中获取
            if (typeProxyInstanceMap.containsKey(fullName)) {
                @Suppress("UNCHECKED_CAST")
                return typeProxyInstanceMap[fullName] as ComposedType<T>
            }
            // 获取合适的构造函数并创建实例
            fun <T> createInstance(clazz: Class<T>): T {
                // 获取空构造函数
                val constructor = clazz.declaredConstructors.find {
                    it.parameterTypes.size == 0
                }
                if (constructor != null) {
                    constructor.isAccessible = true
                    // 创建实例
                    @Suppress("UNCHECKED_CAST")
                    return constructor.newInstance() as T
                }
                throw NoSuchMethodException("没有找到空构造函数: ${clazz.name}")
            }

            // 生成代理类
            val proxyClass = AsmClassTranslation(fullName).createNewClass()
            // 同时生成所有的内部类
            runningClassMapWithoutLibrary.filter { (name, _) -> name.startsWith("$fullName$") }.forEach { (name, _) ->
                AsmClassTranslation(name).createNewClass()
            }
            // 创建实例
            @Suppress("UNCHECKED_CAST")
            val newInstance = createInstance(proxyClass) as ComposedType<T>
            // 缓存实例
            typeProxyInstanceMap[fullName] = newInstance

            return newInstance
        }

    }

}