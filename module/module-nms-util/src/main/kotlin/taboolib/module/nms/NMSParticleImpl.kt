package taboolib.module.nms

import net.minecraft.server.v1_16_R1.PacketPlayOutWorldParticles
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_16_R1.CraftParticle
import org.bukkit.util.Vector

/**
 * TabooLib
 * taboolib.module.nms.NMSParticleImpl
 *
 * @author 坏黑
 * @since 2023/5/2 21:58
 */
class NMSParticleImpl : NMSParticle() {

    override fun createParticlePacket(particle: Particle, location: Location, offset: Vector, speed: Double, count: Int, data: Any?): Any {
        if (data != null && !particle.dataType.isInstance(data)) {
            error("data should be ${particle.dataType} got ${data.javaClass}")
        }
        return PacketPlayOutWorldParticles(
            CraftParticle.toNMS(particle, data),
            true,
            location.x,
            location.y,
            location.z,
            offset.x.toFloat(),
            offset.y.toFloat(),
            offset.z.toFloat(),
            speed.toFloat(),
            count
        )
    }
}