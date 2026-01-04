package taboolib.module.ui.type.storable

import taboolib.platform.util.isAir

/**
 * 放置操作处理器
 * 处理 PLACE_ONE, PLACE_ALL, PLACE_SOME
 */
class PlaceActionHandler : BaseActionHandler() {
    
    /**
     * 放置一个物品（右键）
     */
    fun placeOne(ctx: StorableActionContext): StorableActionResult {
        val cursor = ctx.cursor ?: return StorableActionResult.DENIED
        if (cursor.isAir) return StorableActionResult.DENIED
        if (canPlace(ctx, cursor)) {
            val slotItem = ctx.slotItem
            return when {
                // 空槽位：放 1 个
                slotItem.isAir -> {
                    writeSlot(ctx, cursor.clone().apply { amount = 1 })
                    updateCursor(ctx, cursor, cursor.amount - 1)
                    StorableActionResult.HANDLED
                }
                // 相同物品：合并 1 个
                slotItem != null && slotItem.isSimilar(cursor) -> {
                    val maxStack = getMaxStack(ctx, slotItem)
                    if (slotItem.amount < maxStack) {
                        writeSlot(ctx, slotItem.clone().apply { amount = slotItem.amount + 1 })
                        updateCursor(ctx, cursor, cursor.amount - 1)
                        StorableActionResult.HANDLED
                    } else {
                        StorableActionResult.DENIED
                    }
                }
                else -> StorableActionResult.DENIED
            }
        }
        return StorableActionResult.DENIED
    }
    
    /**
     * 放置全部物品（左键）
     */
    fun placeAll(ctx: StorableActionContext): StorableActionResult {
        val cursor = ctx.cursor ?: return StorableActionResult.DENIED
        if (cursor.isAir) return StorableActionResult.DENIED
        if (canPlace(ctx, cursor)) {
            val slotItem = ctx.slotItem
            return when {
                // 空槽位：放全部
                slotItem.isAir -> {
                    writeSlot(ctx, cursor.clone())
                    setCursor(ctx, null)
                    StorableActionResult.HANDLED
                }
                // 相同物品：合并
                slotItem != null && slotItem.isSimilar(cursor) -> {
                    val maxStack = getMaxStack(ctx, slotItem)
                    val (slotAmount, cursorAmount) = calculateMerge(slotItem.amount, cursor.amount, maxStack)
                    writeSlot(ctx, slotItem.clone().apply { amount = slotAmount })
                    updateCursor(ctx, cursor, cursorAmount)
                    StorableActionResult.HANDLED
                }
                else -> StorableActionResult.DENIED
            }
        }
        return StorableActionResult.DENIED
    }
}
