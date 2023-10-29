package taboolib.module.ui

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Ghost
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.platform.util.isNotAir
import taboolib.platform.util.setMeta

@PlatformSide([Platform.BUKKIT])
internal object ClickListener {

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        Bukkit.getOnlinePlayers().forEach {
            if (MenuHolder.fromInventory(it.openInventory.topInventory) != null) {
                it.closeInventory()
            }
        }
    }

    @SubscribeEvent
    fun onOpen(e: InventoryOpenEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) ?: return
        // 构建回调
        submit {
            builder.buildCallback(e.player as Player, e.inventory)
            builder.selfBuildCallback(e.player as Player, e.inventory)
        }
        // 异步构建回调
        submitAsync {
            builder.asyncBuildCallback(e.player as Player, e.inventory)
            builder.selfAsyncBuildCallback(e.player as Player, e.inventory)
        }
    }

    @SubscribeEvent
    fun onClick(e: InventoryClickEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) ?: return
        // 锁定主手
        if (builder.handLocked && (e.rawSlot - e.inventory.size - 27 == e.whoClicked.inventory.heldItemSlot || e.click == org.bukkit.event.inventory.ClickType.NUMBER_KEY && e.hotbarButton == e.whoClicked.inventory.heldItemSlot)) {
            e.isCancelled = true
        }
        // 处理事件
        try {
            val event = ClickEvent(e, ClickType.CLICK, builder.getSlot(e.rawSlot), builder)
            builder.clickCallback.forEach { it(event) }
            builder.selfClickCallback(event)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        // 如果事件取消则不处理后续逻辑
        if (e.isCancelled) {
            return
        }
        // 丢弃逻辑
        if (e.currentItem.isNotAir() && e.click == org.bukkit.event.inventory.ClickType.DROP) {
            val item = VectorUtil.itemDrop(e.whoClicked as Player, e.currentItem)
            item.pickupDelay = 20
            item.setMeta("internal-drop", true)
            val event = PlayerDropItemEvent((e.whoClicked as Player), item)
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled) {
                event.itemDrop.remove()
            } else {
                e.currentItem?.type = Material.AIR
                e.currentItem = null
            }
        } else if (e.cursor.isNotAir() && e.rawSlot == -999) {
            val item = VectorUtil.itemDrop(e.whoClicked as Player, e.cursor)
            item.pickupDelay = 20
            item.setMeta("internal-drop", true)
            val event = PlayerDropItemEvent((e.whoClicked as Player), item)
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled) {
                event.itemDrop.remove()
            } else {
                e.view.cursor?.type = Material.AIR
                e.view.cursor = null
            }
        }
    }

    @SubscribeEvent
    fun onDrag(e: InventoryDragEvent) {
        val menu = MenuHolder.fromInventory(e.inventory) ?: return
        val clickEvent = ClickEvent(e, ClickType.DRAG, ' ', menu)
        menu.clickCallback.forEach { it.invoke(clickEvent) }
        menu.selfClickCallback(clickEvent)
    }

    @SubscribeEvent
    fun onClose(e: InventoryCloseEvent) {
        val menu = MenuHolder.fromInventory(e.inventory) ?: return
        // 标题更新 && 跳过关闭回调
        if (menu.isUpdateTitle && menu.skipCloseCallbackOnUpdateTitle) {
            return
        }
        menu.closeCallback.invoke(e)
        // 只触发一次
        if (menu.onceCloseCallback) {
            menu.closeCallback = {}
        }
    }

    @SubscribeEvent
    fun onDropItem(e: PlayerDropItemEvent) {
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked && !e.itemDrop.hasMetadata("internal-drop")) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun onItemHeld(e: PlayerItemHeldEvent) {
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked) {
            e.isCancelled = true
        }
    }

    @Ghost
    @SubscribeEvent
    fun onSwap(e: PlayerSwapHandItemsEvent) {
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked) {
            e.isCancelled = true
        }
    }
}