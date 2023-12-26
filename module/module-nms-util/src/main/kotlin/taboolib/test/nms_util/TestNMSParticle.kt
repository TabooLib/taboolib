package taboolib.test.nms_util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.createPacket

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSParticle
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestNMSParticle : Test() {

    override fun check(): List<Result> {
        return listOf(
            sandbox("NMSParticle:createPacket()") {
                try {
                    Particle.CLOUD.createPacket(Location(Bukkit.getWorlds().firstOrNull(), 0.0, 0.0, 0.0))
                } catch (ex: NoClassDefFoundError) {
                    throw UnsupportedVersionException()
                }
            }
        )
    }
}