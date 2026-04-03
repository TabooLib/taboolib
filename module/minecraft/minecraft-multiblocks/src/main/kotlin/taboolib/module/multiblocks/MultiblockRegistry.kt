package taboolib.module.multiblocks

import java.util.concurrent.ConcurrentHashMap

/**
 * 多方块结构全局注册表
 *
 * 管理所有已注册的多方块结构定义。
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
object MultiblockRegistry {

    private val multiblocks = ConcurrentHashMap<String, IMultiblock>()

    /**
     * 注册一个多方块结构
     *
     * @param id 结构唯一标识符
     * @param multiblock 多方块结构实例
     */
    fun register(id: String, multiblock: IMultiblock) {
        multiblock.id = id
        multiblocks[id] = multiblock
    }

    /**
     * 获取已注册的多方块结构
     *
     * @param id 结构标识符
     * @return 多方块结构实例，未注册时返回 null
     */
    fun get(id: String): IMultiblock? {
        return multiblocks[id]
    }

    /**
     * 移除已注册的多方块结构
     *
     * @param id 结构标识符
     * @return 被移除的多方块结构实例，未注册时返回 null
     */
    fun unregister(id: String): IMultiblock? {
        return multiblocks.remove(id)
    }

    /**
     * 获取所有已注册的多方块结构
     */
    fun getAll(): Map<String, IMultiblock> {
        return multiblocks.toMap()
    }

    /**
     * 清空注册表
     */
    fun clear() {
        multiblocks.clear()
    }
}
