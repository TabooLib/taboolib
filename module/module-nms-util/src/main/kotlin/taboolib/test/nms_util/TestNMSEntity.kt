package taboolib.test.nms_util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Villager
import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.module.nms.spawnEntity

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSEntity
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestNMSEntity : Test() {

    override fun check(): List<Result> {
        val worlds = Bukkit.getWorlds()
        if (worlds.isEmpty()) {
            return listOf(Failure.of("AI:NO_WORLD"))
        }
        val world = worlds[0]
        val loc = Location(world, 0.0, 0.0, 0.0)
        return listOf(sandbox("NMSEntity:spawnEntity()") { loc.spawnEntity(Villager::class.java) })
    }
}