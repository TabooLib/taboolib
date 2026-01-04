package taboolib.module.ui.type.impl

import org.bukkit.Material
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryAction.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.t
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.module.ui.ItemStacker
import taboolib.module.ui.type.*
import taboolib.module.ui.type.storable.*
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir
import org.bukkit.event.inventory.ClickType as BukkitClickType

/**
 * 可存储物品的箱子界面实现
 */
open class StorableChestImpl(title: String) : ChestImpl(title), StorableChest {

    /** 页面规则 */
    val rule = RuleImpl()

    /** 是否允许数字键交互 */
    var allowNumberKey = true

    /** 是否自动堆叠物品 */
    var autoStackItems = false

    // 处理器实例
    protected val placeHandler = PlaceActionHandler()
    protected val pickupHandler = PickupActionHandler()
    protected val swapHandler = SwapActionHandler()
    protected val shiftClickHandler = ShiftClickHandler()
    protected val dragHandler = DragActionHandler()

    override fun rule(rule: StorableChest.Rule.() -> Unit) {
        if (virtualized) error(
            """
                无法在虚拟页面中更改规则。
                Cannot change rule when virtualized
            """.t()
        )
        rule(this.rule)
    }

    override fun autoStack(enabled: Boolean) {
        this.autoStackItems = enabled
    }

    override fun onClick(bind: Int, callback: (event: ClickEvent) -> Unit) {
        onClick { if (it.rawSlot == bind) callback(it) }
    }

    override fun onClick(bind: Char, callback: (event: ClickEvent) -> Unit) {
        onClick { if (it.slot == bind) callback(it) }
    }

    override fun onClick(lock: Boolean, callback: (event: ClickEvent) -> Unit) {
        if (lock) {
            clickCallback += {
                it.isCancelled = true
                callback(it)
            }
        } else {
            clickCallback += callback
        }
    }

    override fun build(): Inventory {
        buildRule()
        return super.build()
    }

    open fun buildRule() {
        selfClick {
            if (it.isCancelled) return@selfClick
            when (it.clickType) {
                ClickType.DRAG -> handleDragEvent(it)
                ClickType.CLICK -> handleClickEvent(it)
                else -> {}
            }
        }
    }

    /**
     * 处理拖拽事件
     */
    protected open fun handleDragEvent(event: ClickEvent) {
        // region 拖拽处理
        val rawSlots = event.dragEvent().rawSlots
        if (rawSlots.size == 1) {
            // 单格拖拽，映射为点击行为
            val slot = rawSlots.first()
            if (slot < event.inventory.size) {
                val ctx = DragActionContext(
                    event = event,
                    inventory = event.inventory,
                    slot = slot,
                    oldCursor = event.dragEvent().oldCursor,
                    slotItem = rule.getItem(event.inventory, slot),
                    dragType = event.dragEvent().type,
                    rule = rule
                )
                val result = dragHandler.handleSingleSlotDrag(ctx)
                if (result == StorableActionResult.DENIED) {
                    event.isCancelled = true
                }
            }
        } else {
            // 多格拖拽
            val result = dragHandler.handleMultiSlotDrag(event, event.inventory.size)
            if (result == StorableActionResult.DENIED) {
                event.isCancelled = true
            }
        }
        // endregion
    }

    /**
     * 处理点击事件
     */
    protected open fun handleClickEvent(event: ClickEvent) {
        // region 点击处理
        val action = event.clickEvent().action
        // 阻止双击收集
        if (action == COLLECT_TO_CURSOR) {
            event.isCancelled = true
            return
        }
        val rawSlot = event.rawSlot
        val inventorySize = event.inventory.size
        // 从玩家背包 Shift 点击到 UI
        if (event.clickEvent().click.isShiftClick && rawSlot >= inventorySize) {
            event.isCancelled = true
            val ctx = createContext(event, rawSlot, action)
            shiftClickHandler.shiftClickToUI(ctx, autoStackItems)
            return
        }
        // 点击 UI 内的槽位
        if (rawSlot in 0 until inventorySize) {
            val ctx = createContext(event, rawSlot, action)
            val result = dispatchAction(ctx)
            when (result) {
                StorableActionResult.HANDLED -> event.isCancelled = true
                StorableActionResult.DENIED -> event.isCancelled = true
                StorableActionResult.PASS -> { /* 不取消，让 Bukkit 处理 */
                }
            }
        }
        // endregion
    }

    /**
     * 创建操作上下文
     */
    protected open fun createContext(event: ClickEvent, slot: Int, action: InventoryAction): StorableActionContext {
        return StorableActionContext(
            event = event,
            inventory = event.inventory,
            slot = slot,
            cursor = event.clicker.itemOnCursor.takeIf { it.isNotAir() },
            slotItem = rule.getItem(event.inventory, slot),
            action = action,
            clickType = event.clickEvent().click,
            rule = rule
        )
    }

    /**
     * 根据 InventoryAction 分发到对应处理器
     */
    protected open fun dispatchAction(ctx: StorableActionContext): StorableActionResult {
        // @formatter:off
        val result = when (ctx.action) {
            // 放置操作
            PLACE_ONE -> placeHandler.placeOne(ctx)
            PLACE_ALL -> placeHandler.placeAll(ctx)
            PLACE_SOME -> placeHandler.placeAll(ctx)
            // 取出操作
            PICKUP_ONE -> pickupHandler.pickupOne(ctx)
            PICKUP_HALF -> pickupHandler.pickupHalf(ctx)
            PICKUP_ALL -> pickupHandler.pickupAll(ctx)
            // 交换操作
            SWAP_WITH_CURSOR -> swapHandler.swapWithCursor(ctx)
            HOTBAR_SWAP -> {
                if (allowNumberKey) swapHandler.hotbarSwap(ctx)
                else StorableActionResult.DENIED
            }
            HOTBAR_MOVE_AND_READD -> {
                if (allowNumberKey) swapHandler.hotbarSwap(ctx)
                else StorableActionResult.DENIED
            }
            // Shift 点击（从 UI 到背包）
            MOVE_TO_OTHER_INVENTORY -> shiftClickHandler.shiftClickFromUI(ctx)
            // 其他
            COLLECT_TO_CURSOR -> StorableActionResult.DENIED
            CLONE_STACK -> StorableActionResult.DENIED
            DROP_ONE_SLOT,
            DROP_ALL_SLOT -> handleDropFromSlot(ctx)
            DROP_ONE_CURSOR,
            DROP_ALL_CURSOR -> StorableActionResult.PASS
            NOTHING -> StorableActionResult.PASS
            else -> StorableActionResult.PASS
        }
        // @formatter:on
        return result
    }

    /**
     * 处理从槽位丢弃物品
     */
    protected open fun handleDropFromSlot(ctx: StorableActionContext): StorableActionResult {
        val slotItem = ctx.slotItem ?: return StorableActionResult.DENIED
        if (slotItem.isAir) return StorableActionResult.DENIED
        // 检查是否允许取出（装饰物保护）
        if (!ctx.rule.canPlace(ctx.inventory, slotItem, ctx.slot)) {
            return StorableActionResult.DENIED
        }
        // 手动处理丢弃，确保触发 writeItem
        val player = ctx.player
        val dropAmount = if (ctx.action == DROP_ONE_SLOT) 1 else slotItem.amount
        // 更新槽位
        if (dropAmount >= slotItem.amount) {
            rule.setItem(ctx.inventory, ItemStack(Material.AIR), ctx.slot, ctx.clickType)
        } else {
            rule.setItem(ctx.inventory, slotItem.clone().apply { amount = slotItem.amount - dropAmount }, ctx.slot, ctx.clickType)
        }
        // 丢弃物品到世界
        val dropItem = slotItem.clone().apply { amount = dropAmount }
        player.world.dropItem(player.eyeLocation, dropItem).apply {
            velocity = player.location.direction.multiply(0.3)
        }
        return StorableActionResult.HANDLED
    }

    /**
     * 规则实现类
     */
    class RuleImpl : StorableChest.Rule {

        private var checkSlotCallback: ((Inventory, ItemStack, Int) -> Boolean) = { _, _, _ -> true }
        private var firstSlotCallback: ((Inventory, ItemStack) -> Int) = { _, _ -> -1 }
        private var writeItemCallback: ((Inventory, ItemStack, Int, BukkitClickType) -> Unit) = { inv, item, slot, _ ->
            if (slot in 0 until inv.size) inv.setItem(slot, item)
        }
        private var readItemCallback: ((Inventory, Int) -> ItemStack?) = { inv, slot ->
            if (slot in 0 until inv.size) inv.getItem(slot) else null
        }
        private var shiftSwapCallback: ((Inventory, ItemStack, Int) -> Boolean) = { _, _, _ -> false }
        private var mergeSlotsCallback: ((Inventory, ItemStack) -> List<Int>)? = null

        var itemStackerValue: ItemStacker = ItemStacker.MINECRAFT

        // ============ 内部读取方法（Handler 使用）============

        fun canPlace(inventory: Inventory, item: ItemStack, slot: Int): Boolean {
            return checkSlotCallback(inventory, item, slot)
        }

        fun getItem(inventory: Inventory, slot: Int): ItemStack? {
            return readItemCallback(inventory, slot)
        }

        fun setItem(inventory: Inventory, item: ItemStack, slot: Int, clickType: BukkitClickType) {
            writeItemCallback(inventory, item, slot, clickType)
        }

        fun getFirstSlot(inventory: Inventory, item: ItemStack): Int {
            return firstSlotCallback(inventory, item)
        }

        fun getMergeSlots(inventory: Inventory, item: ItemStack): List<Int>? {
            return mergeSlotsCallback?.invoke(inventory, item)
        }

        fun canShiftSwap(inventory: Inventory, item: ItemStack, slot: Int): Boolean {
            return shiftSwapCallback(inventory, item, slot)
        }

        fun getItemStacker(): ItemStacker = itemStackerValue

        // ============ 公开配置方法（用户使用）============

        override fun checkSlot(intRange: Int, checkSlot: (Inventory, ItemStack) -> Boolean) {
            checkSlot(intRange..intRange, checkSlot)
        }

        override fun checkSlot(intRange: IntRange, callback: (Inventory, ItemStack) -> Boolean) {
            val before = checkSlotCallback
            checkSlotCallback = { inventory, itemStack, slot ->
                if (slot in intRange) callback(inventory, itemStack)
                else before(inventory, itemStack, slot)
            }
        }

        override fun checkSlot(callback: (Inventory, ItemStack, Int) -> Boolean) {
            val before = checkSlotCallback
            checkSlotCallback = { inventory, itemStack, slot ->
                callback(inventory, itemStack, slot) && before(inventory, itemStack, slot)
            }
        }

        override fun firstSlot(firstSlot: (Inventory, ItemStack) -> Int) {
            this.firstSlotCallback = firstSlot
        }

        override fun writeItem(writeItem: (Inventory, ItemStack, Int) -> Unit) {
            this.writeItemCallback = { inventory, itemStack, slot, _ -> writeItem(inventory, itemStack, slot) }
        }

        override fun writeItem(writeItem: (Inventory, ItemStack, Int, BukkitClickType) -> Unit) {
            this.writeItemCallback = writeItem
        }

        override fun readItem(readItem: (Inventory, Int) -> ItemStack?) {
            this.readItemCallback = readItem
        }

        override fun shiftSwap(shiftSwap: (Inventory, ItemStack, Int) -> Boolean) {
            this.shiftSwapCallback = shiftSwap
        }

        override fun itemStacker(itemStacker: ItemStacker) {
            this.itemStackerValue = itemStacker
        }

        override fun mergeSlots(mergeSlots: (Inventory, ItemStack) -> List<Int>) {
            this.mergeSlotsCallback = mergeSlots
        }
    }
}