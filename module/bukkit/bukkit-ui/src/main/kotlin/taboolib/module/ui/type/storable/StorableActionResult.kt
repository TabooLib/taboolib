package taboolib.module.ui.type.storable

/**
 * 操作结果
 */
enum class StorableActionResult {
    /** 允许操作（已处理，取消原版事件） */
    HANDLED,
    /** 拒绝操作（取消事件，不做任何处理） */
    DENIED,
    /** 透传（不取消事件，让 Bukkit 处理） */
    PASS
}
