package taboolib.common.platform.event

/**
 * Hytale ECS 事件上下文接口
 * @author sky
 */
interface HytaleEcsContext<CHUNK, STORE, CMD> {

    /** 实体在 ArchetypeChunk 中的索引 */
    val index: Int

    /** ArchetypeChunk */
    val archetypeChunk: CHUNK

    /** Store */
    val store: STORE

    /** CommandBuffer */
    val commandBuffer: CMD
}
