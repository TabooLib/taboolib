package taboolib.platform.event

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEventBus
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.platform.util.*

/**
 * 将玩家的交互行为统一成单个事件，方便处理。
 *
 * @property player 交互的玩家
 * @property action 交互的动作
 */
class PlayerWorldContactEvent(val player: Player, val action: Action) : CancelableInternalEvent() {

    /** 是否为左键点击（包括左键方块和左键实体） */
    val isLeftClick = action is Action.LeftClickBlock || action is Action.LeftClickEntity

    /** 是否为左键点击空气 */
    val isLeftClickAir = action is Action.LeftClickBlock && action.source.isLeftClickAir()

    /** 是否为左键点击非空气 */
    val isLeftClickNotAir = action is Action.LeftClickBlock && action.source.isLeftClickBlock()

    /** 是否为左键点击方块 */
    val isLeftClickBlock = action is Action.LeftClickBlock

    /** 是否为右键点击（包括右键方块和右键实体） */
    val isRightClick = action is Action.RightClickBlock || action is Action.RightClickEntity

    /** 是否为右键点击空气 */
    val isRightClickAir = action is Action.RightClickBlock && action.source.isRightClickAir()

    /** 是否为右键点击非空气 */
    val isRightClickNotAir = action is Action.RightClickBlock && action.source.isRightClickBlock()

    /** 是否为右键点击方块 */
    val isRightClickBlock = action is Action.RightClickBlock

    /** 是否为左键点击实体 */
    val isLeftClickEntity = action is Action.LeftClickEntity

    /** 是否为右键点击实体 */
    val isRightClickEntity = action is Action.RightClickEntity

    /** 是否为物理交互（如踩压力板） */
    val isPhysical = action is Action.Physical

    /** 是否使用主手 */
    val isMainHand = action.hand == EquipmentSlot.HAND

    /** 是否使用副手 */
    val isOffHand = action.hand == EquipmentSlot.OFF_HAND

    /**
     * 交互动作的密封类
     *
     * @property hand 使用的手
     */
    sealed class Action(open val hand: EquipmentSlot) {

        /**
         * 左键点击方块
         *
         * @property hand 使用的手
         * @property block 点击的方块
         * @property blockFace 点击的方块面
         * @property source 原始的 [PlayerInteractEvent]
         */
        data class LeftClickBlock(
            override val hand: EquipmentSlot,
            val block: Block?,
            val blockFace: BlockFace,
            val source: PlayerInteractEvent
        ) : Action(hand)

        /**
         * 右键点击方块
         *
         * @property hand 使用的手
         * @property block 点击的方块
         * @property blockFace 点击的方块面
         * @property source 原始的 [PlayerInteractEvent]
         */
        data class RightClickBlock(
            override val hand: EquipmentSlot,
            val block: Block?,
            val blockFace: BlockFace,
            val source: PlayerInteractEvent
        ) : Action(hand)

        /**
         * 左键点击实体
         *
         * @property hand 使用的手
         * @property entity 点击的实体
         * @property source 原始的 [EntityDamageByEntityEvent]
         */
        data class LeftClickEntity(
            override val hand: EquipmentSlot,
            val entity: Entity,
            val source: EntityDamageByEntityEvent
        ) : Action(hand)

        /**
         * 右键点击实体
         *
         * @property hand 使用的手
         * @property entity 点击的实体
         * @property position 点击的位置
         * @property source 原始的 [PlayerInteractAtEntityEvent]
         */
        data class RightClickEntity(
            override val hand: EquipmentSlot,
            val entity: Entity,
            val position: Vector,
            val source: PlayerInteractAtEntityEvent
        ) : Action(hand)

        /**
         * 表示玩家的物理交互动作，如踩压力板
         * @param block 触发物理交互的方块
         * @param blockFace 触发物理交互的方块面
         * @param source 原始的 [PlayerInteractEvent]
         */
        data class Physical(
            val block: Block?,
            val blockFace: BlockFace,
            val source: PlayerInteractEvent
        ) : Action(EquipmentSlot.FEET)
    }

    @Inject
    @PlatformSide(Platform.BUKKIT)
    companion object {

        /**
         * 使用优先级
         */
        var usePriority = EventPriority.NORMAL

        /**
         * 是否忽略已取消的事件
         */
        var ignoreCancelled = true

        /**
         * 本插件是否监听了 [PlayerWorldContactEvent] 事件
         */
        val isListened: Boolean
            get() = InternalEventBus.isListening(PlayerWorldContactEvent::class.java)

        @Awake(LifeCycle.ENABLE)
        private fun onEnable() {
            if (!isListened) return
            // 左键交互实体（造成伤害）
            registerBukkitListener(EntityDamageByEntityEvent::class.java, usePriority, ignoreCancelled) { e ->
                val player = e.damager as? Player ?: return@registerBukkitListener
                // 仅限左键常规攻击
                if (e.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                    // 交互事件
                    if (!PlayerWorldContactEvent(player, Action.LeftClickEntity(EquipmentSlot.HAND, e.entity, e)).callIf()) {
                        e.isCancelled = true
                    }
                }
            }
            // 左键、右键、物理交互方块
            registerBukkitListener(PlayerInteractEvent::class.java, usePriority, ignoreCancelled) { e ->
                val action = when {
                    e.isRightClick() -> Action.RightClickBlock(e.hand ?: EquipmentSlot.HAND, e.clickedBlock, e.blockFace, e)
                    e.isLeftClick() -> Action.LeftClickBlock(e.hand ?: EquipmentSlot.HAND, e.clickedBlock, e.blockFace, e)
                    e.isPhysical() -> Action.Physical(e.clickedBlock, e.blockFace, e)
                    else -> return@registerBukkitListener
                }
                if (!PlayerWorldContactEvent(e.player, action).callIf()) {
                    e.isCancelled = true
                }
            }
            // 右键交互实体
            registerBukkitListener(PlayerInteractAtEntityEvent::class.java, usePriority, ignoreCancelled) { e ->
                if (!PlayerWorldContactEvent(e.player, Action.RightClickEntity(e.hand, e.rightClicked, e.clickedPosition, e)).callIf()) {
                    e.isCancelled = true
                }
            }
        }
    }
}