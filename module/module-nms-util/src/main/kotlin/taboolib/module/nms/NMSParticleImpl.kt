package taboolib.module.nms

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

/**
 * TabooLib
 * taboolib.module.nms.NMSParticleImpl
 *
 * @author 坏黑
 * @since 2023/5/2 21:58
 */
class NMSParticleImpl : NMSParticle() {

    val version = MinecraftVersion.majorLegacy

    override fun createParticlePacket(particle: Particle, location: Location, offset: Vector, speed: Double, count: Int, data: Any?): Any {
        if (data != null && !particle.dataType.isInstance(data)) {
            error("data should be ${particle.dataType} got ${data.javaClass}")
        }
        return if (version >= 11200) {
            net.minecraft.server.v1_16_R1.PacketPlayOutWorldParticles(
                org.bukkit.craftbukkit.v1_16_R1.CraftParticle.toNMS(particle, data),
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
        } else {
            net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles(
                org.bukkit.craftbukkit.v1_12_R1.CraftParticle.toNMS(particle),
                true,
                location.x.toFloat(),
                location.y.toFloat(),
                location.z.toFloat(),
                offset.x.toFloat(),
                offset.y.toFloat(),
                offset.z.toFloat(),
                speed.toFloat(),
                count
            )
        }
    }
}