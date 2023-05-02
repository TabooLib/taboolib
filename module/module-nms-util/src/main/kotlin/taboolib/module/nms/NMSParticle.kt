package taboolib.module.nms

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import taboolib.common.util.unsafeLazy

/**
 * TabooLib
 * taboolib.module.nms.NMSParticle
 *
 * @author 坏黑
 * @since 2023/5/2 21:57
 */
abstract class NMSParticle {

    abstract fun createParticlePacket(particle: Particle, location: Location, offset: Vector = Vector(), speed: Double = 0.0, count: Int = 1, data: Any? = null): Any

    companion object {

        val instance by unsafeLazy { nmsProxy<NMSParticle>() }

        /** 创建粒子数据包 */
        fun Particle.createPacket(location: Location, offset: Vector = Vector(), speed: Double = 0.0, count: Int = 1, data: Any? = null): Any {
            return instance.createParticlePacket(this, location, offset, speed, count, data)
        }
    }
}