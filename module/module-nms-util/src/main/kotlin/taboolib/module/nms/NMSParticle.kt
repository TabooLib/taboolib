package taboolib.module.nms

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import taboolib.common.UnsupportedVersionException

/**
 * 通过 [Particle] 创建粒子数据包
 *
 * @param location 粒子位置
 * @param offset 粒子偏移
 * @param speed 粒子速度
 * @param count 粒子数量
 * @param data 粒子数据
 */
fun Particle.createPacket(location: Location, offset: Vector = Vector(), speed: Double = 0.0, count: Int = 1, data: Any? = null): Any {
    return nmsProxy<NMSParticle>().createParticlePacket(this, location, offset, speed, count, data)
}

/**
 * TabooLib
 * taboolib.module.nms.NMSParticle
 *
 * @author 坏黑
 * @since 2023/5/2 21:57
 */
abstract class NMSParticle {

    /** 创建粒子数据包 */
    abstract fun createParticlePacket(particle: Particle, location: Location, offset: Vector = Vector(), speed: Double = 0.0, count: Int = 1, data: Any? = null): Any
}

/**
 * [NMSParticle] 的实现类
 */
class NMSParticleImpl : NMSParticle() {

    val version = MinecraftVersion.majorLegacy

    override fun createParticlePacket(particle: Particle, location: Location, offset: Vector, speed: Double, count: Int, data: Any?): Any {
        if (data != null && !particle.dataType.isInstance(data)) {
            error("data should be ${particle.dataType} (${data.javaClass})")
        }
        return if (MinecraftVersion.isHigher(MinecraftVersion.V1_12)) {
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