package taboolib.module.kether.action.game.bukkit

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.OpenResult
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.module.kether.*

@PlatformSide([Platform.BUKKIT])
object ActionItemStack {

    @KetherParser(["item", "itemstack"])
    fun actionItemStack() = scriptParser {
        val str = it.nextParsedAction()
        actionTake { run(str).str { s -> ItemStack(Material.getMaterial(s.uppercase()) ?: error("Unknown material: $s")) } }
    }

    @KetherParser(["material"])
    fun actionMaterial() = scriptParser {
        val str = it.nextParsedAction()
        actionTake { run(str).str { s -> Material.getMaterial(s.uppercase()) ?: error("Unknown material: $s") } }
    }

    @KetherProperty(bind = ItemStack::class)
    fun propertyItemStack() = object : ScriptProperty<ItemStack>("item.operator") {

        override fun read(instance: ItemStack, key: String): OpenResult {
            return when (key) {
                "type", "material" -> OpenResult.successful(instance.type)
                "meta", "itemmeta" -> OpenResult.successful(instance.itemMeta)
                "data", "damage", "durability" -> OpenResult.successful(instance.durability)
                "name" -> OpenResult.successful(instance.itemMeta?.displayName)
                "lore" -> OpenResult.successful(instance.itemMeta?.lore ?: arrayListOf<String>())
                else -> OpenResult.failed()
            }
        }

        override fun write(instance: ItemStack, key: String, value: Any?): OpenResult {
            return when (key) {
                "type", "material" -> {
                    instance.type = (value as? Material) ?: Material.getMaterial(value.toString().uppercase()) ?: error("Unknown material: $value")
                    OpenResult.successful()
                }
                "meta", "itemmeta" -> {
                    instance.itemMeta = value as ItemMeta
                    OpenResult.successful()
                }
                "data", "damage", "durability" -> {
                    instance.durability = Coerce.toShort(value)
                    OpenResult.successful()
                }
                "name" -> {
                    val meta = instance.itemMeta
                    meta?.setDisplayName(value.toString())
                    instance.itemMeta = meta
                    OpenResult.successful()
                }
                "lore" -> {
                    val meta = instance.itemMeta
                    meta?.lore = value?.asList() ?: emptyList()
                    instance.itemMeta = meta
                    OpenResult.successful()
                }
                else -> OpenResult.failed()
            }
        }
    }

    @KetherProperty(bind = ItemMeta::class)
    fun propertyItemMeta() = object : ScriptProperty<ItemMeta>("itemMeta.operator") {

        override fun read(instance: ItemMeta, key: String): OpenResult {
            return when (key) {
                "name" -> OpenResult.successful(instance.displayName)
                "lore" -> OpenResult.successful(instance.lore ?: arrayListOf<String>())
                else -> OpenResult.failed()
            }
        }

        override fun write(instance: ItemMeta, key: String, value: Any?): OpenResult {
            return when (key) {
                "name" -> {
                    instance.setDisplayName(value.toString())
                    OpenResult.successful()
                }
                "lore" -> {
                    instance.lore = value?.asList() ?: emptyList()
                    OpenResult.successful()
                }
                else -> {
                    OpenResult.failed()
                }
            }
        }
    }
}