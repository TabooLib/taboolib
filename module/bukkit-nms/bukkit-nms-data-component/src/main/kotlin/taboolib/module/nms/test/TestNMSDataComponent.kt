package taboolib.module.nms.test

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.Test
import taboolib.module.chat.Components
import taboolib.module.chat.StandardColors
import taboolib.module.nms.component.ComposedItem
import taboolib.module.nms.component.ItemComponents
import taboolib.module.nms.component.composed
import taboolib.platform.util.buildItem

/**
 * TabooLib
 * taboolib.module.nms.test.TestNMSDataComponent
 *
 * @author mical
 * @since 2026/2/19 11:12
 */
object TestNMSDataComponent : Test() {

    override fun check(): List<Result> {
        val result = arrayListOf<Result>()
        var composed: ComposedItem? = null
        // composed
        result += sandbox("NMSDataComponent::composed") { composed = item().composed() }

        // CustomModelData
        result += sandbox("NMSDataComponent:getItemModelData") { composed!!.get(ItemComponents.CUSTOM_MODEL_DATA) }
        result += sandbox("NMSDataComponent:hasItemModelData") { composed!!.has(ItemComponents.CUSTOM_MODEL_DATA) }
        result += sandbox("NMSDataComponent:removeItemModelData") { composed!!.remove(ItemComponents.CUSTOM_MODEL_DATA) }
        result += sandbox("NMSDataComponent:setItemModelData") { composed!!.set(ItemComponents.CUSTOM_MODEL_DATA, 99) }

        // CustomName
        result += sandbox("NMSDataComponent:getCustomName") { composed!!.get(ItemComponents.CUSTOM_NAME) }
        result += sandbox("NMSDataComponent:hasCustomName") { composed!!.has(ItemComponents.CUSTOM_NAME) }
        result += sandbox("NMSDataComponent:removeCustomName") { composed!!.remove(ItemComponents.CUSTOM_NAME) }
        result += sandbox("NMSDataComponent:setCustomName") { composed!!.set(ItemComponents.CUSTOM_NAME, Components.text("sb").color(
            StandardColors.LIGHT_PURPLE)) }

        // Damage
        result += sandbox("NMSDataComponent:getDamage") { composed!!.get(ItemComponents.DAMAGE) }
        result += sandbox("NMSDataComponent:hasDamage") { composed!!.has(ItemComponents.DAMAGE) }
        result += sandbox("NMSDataComponent:removeDamage") { composed!!.has(ItemComponents.DAMAGE) }
        result += sandbox("NMSDataComponent:setDamage") { composed!!.set(ItemComponents.DAMAGE, 10) }

        // EnchantmentGlintOverride
        result += sandbox("NMSDataComponent:getEnchantmentGlintOverride") { composed!!.get(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE) }
        result += sandbox("NMSDataComponent:hasEnchantmentGlintOverride") { composed!!.has(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE) }
        result += sandbox("NMSDataComponent:removeEnchantmentGlintOverride") { composed!!.remove(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE) }
        result += sandbox("NMSDataComponent:setEnchantmentGlintOverride") { composed!!.set(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE, false) }

        // ItemName
        result += sandbox("NMSDataComponent:getItemName") { composed!!.get(ItemComponents.ITEM_NAME) }
        result += sandbox("NMSDataComponent:hasItemName") { composed!!.has(ItemComponents.ITEM_NAME) }
        result += sandbox("NMSDataComponent:removeItemName") { composed!!.remove(ItemComponents.ITEM_NAME) }
        result += sandbox("NMSDataComponent:setItemName") { composed!!.set(ItemComponents.ITEM_NAME, Components.text("泥土")) }

        // Lore
        result += sandbox("NMSDataComponent:getLore") { composed!!.get(ItemComponents.LORE) }
        result += sandbox("NMSDataComponent:hasLore") { composed!!.has(ItemComponents.LORE) }
        result += sandbox("NMSDataComponent:removeLore") { composed!!.remove(ItemComponents.LORE) }
        result += sandbox("NMSDataComponent:setLore") { composed!!.set(ItemComponents.LORE, mutableListOf(Components.text("测试"))) }

        // MaxDamage
        result += sandbox("NMSDataComponent:getMaxDamage") { composed!!.get(ItemComponents.MAX_DAMAGE) }
        result += sandbox("NMSDataComponent:hasMaxDamage") { composed!!.has(ItemComponents.MAX_DAMAGE) }
        result += sandbox("NMSDataComponent:removeMaxDamage") { composed!!.remove(ItemComponents.MAX_DAMAGE) }
        result += sandbox("NMSDataComponent:setMaxDamage") { composed!!.set(ItemComponents.MAX_DAMAGE, 20) }

        // MaxStackSize
        result += sandbox("NMSDataComponent:getMaxStackSize") { composed!!.get(ItemComponents.MAX_STACK_SIZE) }
        result += sandbox("NMSDataComponent:hasMaxStackSize") { composed!!.has(ItemComponents.MAX_STACK_SIZE) }
        result += sandbox("NMSDataComponent:removeMaxStackSize") { composed!!.remove(ItemComponents.MAX_STACK_SIZE) }
        result += sandbox("NMSDataComponent:setMaxStackSize") { composed!!.set(ItemComponents.MAX_STACK_SIZE, 20) }

        // RepairCost
        result += sandbox("NMSDataComponent:getRepairCost") { composed!!.get(ItemComponents.REPAIR_COST) }
        result += sandbox("NMSDataComponent:hasRepairCost") { composed!!.has(ItemComponents.REPAIR_COST) }
        result += sandbox("NMSDataComponent:removeRepairCost") { composed!!.remove(ItemComponents.REPAIR_COST) }
        result += sandbox("NMSDataComponent:setRepairCost") { composed!!.set(ItemComponents.REPAIR_COST, 20) }

        // Unbreakable
        result += sandbox("NMSDataComponent:getUnbreakable") { composed!!.get(ItemComponents.UNBREAKABLE) }
        result += sandbox("NMSDataComponent:hasUnbreakable") { composed!!.has(ItemComponents.UNBREAKABLE) }
        result += sandbox("NMSDataComponent:removeUnbreakable") { composed!!.remove(ItemComponents.UNBREAKABLE) }
        result += sandbox("NMSDataComponent:setUnbreakable") { composed!!.set(ItemComponents.UNBREAKABLE, true) }

        return result
    }

    fun item(): ItemStack {
        return buildItem(Material.STONE) {
            name = "&e坏黑"
            lore += "&b牛逼"
            colored()
            customModelData = 10086
            shiny()
            damage = 10
        }
    }
}