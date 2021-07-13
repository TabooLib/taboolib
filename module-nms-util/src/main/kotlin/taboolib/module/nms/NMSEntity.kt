package taboolib.module.nms

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import taboolib.common.Isolated
import java.util.*

/**
 * @author sky
 * @since 2021/7/13 7:26 下午
 */
@Isolated
interface NMSEntity {

    fun spawnItem(player: Player, entityId: Int, uuid: UUID, location: Location, itemStack: ItemStack)

    fun spawnArmorStand(player: Player, entityId: Int, uuid: UUID, location: Location)

    fun destroyEntity(player: Player, entityId: Int)

    fun teleportEntity(player: Player, entityId: Int, location: Location)

    fun updateEquipment(player: Player, entityId: Int, slot: EquipmentSlot, itemStack: ItemStack)

    fun updateEntityMetadata(player: Player, entityId: Int, vararg objects: Any)

    fun getMetaEntityInt(index: Int, value: Int): Any

    fun getMetaEntityFloat(index: Int, value: Float): Any

    fun getMetaEntityString(index: Int, value: String): Any

    fun getMetaEntityBoolean(index: Int, value: Boolean): Any

    fun getMetaEntityByte(index: Int, value: Byte): Any

    fun getMetaEntityVector(index: Int, value: EulerAngle): Any

    fun getMetaEntityChatBaseComponent(index: Int, name: String?): Any

    fun getMetaItem(index: Int, itemStack: ItemStack): Any
}