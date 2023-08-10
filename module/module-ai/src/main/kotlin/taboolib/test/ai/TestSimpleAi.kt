package taboolib.test.ai

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.module.ai.*

/**
 * TabooLib
 * taboolib.test.TestSimpleAi
 *
 * @author 坏黑
 * @since 2023/8/4 02:32
 */
@Isolated
object TestSimpleAi : Test() {

    override fun check(): List<Result> {
        val worlds = Bukkit.getWorlds()
        if (worlds.isEmpty()) {
            return listOf(Failure.of("AI:NO_WORLD"))
        }
        val results = arrayListOf<Result>()
        val world = worlds[0]
        // 生成实体
        val villager = world.spawnEntity(Location(world, 0.0, 0.0, 0.0), EntityType.VILLAGER) as Villager
        try {
            villager.isInvulnerable = true
        } catch (_: Throwable) {
        }
        // 测试功能
        results += sandbox("AI:clearAi()") {
            villager.clearGoalAi()
            villager.clearTargetAi()
        }
        results += sandbox("AI:navigationMove(Location)") {
            villager.navigationMove(villager.location)
        }
        results += sandbox("AI:navigationMove(LivingEntity)") {
            villager.navigationMove(villager)
        }
        results += sandbox("AI:navigationReach()") {
            villager.navigationReach()
        }
        results += sandbox("AI:controllerLookAt(Location)") {
            villager.controllerLookAt(villager.location)
        }
        results += sandbox("AI:controllerLookAt(Entity)") {
            villager.controllerLookAt(villager)
        }
        results += sandbox("AI:controllerJumpReady()") {
            villager.controllerJumpReady()
        }
        results += sandbox("AI:controllerJumpCurrent()") {
            villager.controllerJumpCurrent()
        }
        villager.remove()
        return results
    }
}