package taboolib.module.nms.test

import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.common.Test
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.getI18nName
import taboolib.platform.util.buildItem
import taboolib.platform.util.modifyMeta

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSI18n
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
object TestNMSTranslate : Test() {

    @Suppress("removal")
    override fun check(): List<Result> {
        val worlds = Bukkit.getWorlds()
        if (worlds.isEmpty()) {
            return listOf(Failure.of("NMSTranslate:NO_WORLD"))
        }
        val items = listOf(
            XMaterial.AIR.parseItem()!!, // 空气
            XMaterial.STONE.parseItem()!!, // 石头
            XMaterial.GRANITE.parseItem()!!, // 花岗岩
            XMaterial.WHITE_WOOL.parseItem()!!, // 普通羊毛
            XMaterial.RED_WOOL.parseItem()!!, // 染色羊毛
            XMaterial.ZOMBIE_SPAWN_EGG.parseItem()!!, // 生物蛋
            XMaterial.ZOMBIE_HEAD.parseItem()!!, // 头颅
            XMaterial.PLAYER_HEAD.parseItem()!!, // 玩家头颅
            buildItem(XMaterial.PLAYER_HEAD) { // 玩家头颅
                skullOwner = "bukkitObj"
            },
            buildItem(XMaterial.POTION) {  // 药水
                runCatching { potionData = PotionData(PotionType.FIRE_RESISTANCE, false, false) }.getOrElse { damage = 1 }
            },
            buildItem(XMaterial.SPLASH_POTION) { // 药水（喷溅）
                runCatching { potionData = PotionData(PotionType.FIRE_RESISTANCE, false, false) }.getOrElse { damage = 1 }
            },
            XMaterial.WRITTEN_BOOK.parseItem()!!.modifyMeta<BookMeta> { // 成书
                title = "测试书"
                author = "bukkitObj"
            }
        )
        val world = worlds[0]
        return listOf(
            sandbox("NMSTranslate:getI18nName(ItemStack)") {
                assert(items.count { it.getI18nName() == "NO_LOCALE" })
            },
            sandbox("NMSTranslate:getI18nName(Enchantment)") {
                assert(Enchantment.values().count { it.getI18nName() == "NO_LOCALE" })
            },
            sandbox("NMSTranslate:getI18nName(PotionEffectType)") {
                assert(PotionEffectType.values().filterNotNull().count { it.getI18nName() == "NO_LOCALE" })
            },
            sandbox("NMSTranslate:getI18nName(Entity)") {
                val entity1 = world.spawnEntity(world.spawnLocation, EntityType.ZOMBIE)
                val entity2 = world.spawnEntity(world.spawnLocation, EntityType.VILLAGER) as Villager
                val entity3 = world.spawnEntity(world.spawnLocation, EntityType.VILLAGER) as Villager
                entity3.profession = Villager.Profession.FARMER
                assert(listOf(entity1, entity2, entity3).count { it.getI18nName() == "NO_LOCALE" })
            },
        )
    }

    private fun assert(value: Int) {
        if (value > 0) error(value)
    }
}