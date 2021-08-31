package taboolib.module.ui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.OptionalEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.isNotAir

@PlatformSide([Platform.BUKKIT])
object ClickListener {

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        Bukkit.getOnlinePlayers().forEach {
            if (MenuHolder.fromInventory(it.openInventory.topInventory) != null) {
                it.closeInventory()
            }
        }
    }

    @SubscribeEvent
    fun e(e: InventoryOpenEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) ?: return
        submit {
            builder.onBuild(e.player as Player, e.inventory)
        }
        submit(async = true) {
            builder.onBuildAsync(e.player as Player, e.inventory)
        }
    }

    @SubscribeEvent
    fun e(e: InventoryClickEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) ?: return
        // lock hand
        if (builder.handLocked && (e.rawSlot - e.inventory.size - 27 == e.whoClicked.inventory.heldItemSlot || e.click == org.bukkit.event.inventory.ClickType.NUMBER_KEY && e.hotbarButton == e.whoClicked.inventory.heldItemSlot)) {
            e.isCancelled = true
        }
        try {
            val event = ClickEvent(e, ClickType.CLICK, builder.getSlot(e.rawSlot))
            builder.onClick.forEach { it.accept(event) }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        if (e.isCancelled) {
            return
        }
        // drop on empty area
        if (e.currentItem.isNotAir() && e.click == org.bukkit.event.inventory.ClickType.DROP) {
            val item = VectorUtil.itemDrop(e.whoClicked as Player, e.currentItem)
            item.pickupDelay = 20
            item.setMetadata("internal-drop", FixedMetadataValue(BukkitPlugin.getInstance(), true))
            val event = PlayerDropItemEvent((e.whoClicked as Player), item)
            if (event.isCancelled) {
                event.itemDrop.remove()
            } else {
                e.currentItem = null
            }
        } else if (e.cursor.isNotAir() && e.rawSlot == -999) {
            val item = VectorUtil.itemDrop(e.whoClicked as Player, e.cursor)
            item.pickupDelay = 20
            item.setMetadata("internal-drop", FixedMetadataValue(BukkitPlugin.getInstance(), true))
            val event = PlayerDropItemEvent((e.whoClicked as Player), item)
            if (event.isCancelled) {
                event.itemDrop.remove()
            } else {
                e.view.cursor = null
            }
        }
    }

    @SubscribeEvent
    fun e(e: InventoryDragEvent) {
        val clickEvent = ClickEvent(e, ClickType.DRAG, ' ')
        MenuHolder.fromInventory(e.inventory)?.onClick?.forEach {
            it.accept(clickEvent)
        }
    }

    @SubscribeEvent
    fun e(e: InventoryCloseEvent) {
        MenuHolder.fromInventory(e.inventory)?.onClose?.invoke(e)
    }

    @SubscribeEvent
    fun e(e: PlayerDropItemEvent) {
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked && !e.itemDrop.hasMetadata("internal-drop")) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerItemHeldEvent) {
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent(bind = "org.bukkit.event.player.PlayerSwapHandItemsEvent")
    fun onSwap(ope: OptionalEvent) {
        val e = ope.get<PlayerSwapHandItemsEvent>()
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked) {
            e.isCancelled = true
        }
    }
}