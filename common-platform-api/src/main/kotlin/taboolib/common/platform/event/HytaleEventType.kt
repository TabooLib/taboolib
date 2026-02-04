package taboolib.common.platform.event

/**
 * Hytale 事件订阅类型（用于 @SubscribeEvent 注解）
 * @author sky
 */
enum class HytaleEventType {

    /** 普通事件 (需要指定 key) */
    NORMAL,

    /** 全局事件 (订阅所有 key) */
    GLOBAL,

    /** 未处理事件 */
    UNHANDLED,

    /** 异步事件 (需要指定 key) */
    ASYNC,

    /** 异步全局事件 */
    ASYNC_GLOBAL,

    /** 异步未处理事件 */
    ASYNC_UNHANDLED,

    /** ECS 实体事件 */
    ECS
}
