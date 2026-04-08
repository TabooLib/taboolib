package taboolib.module.nms

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.craftbukkit.CraftParticle
import org.bukkit.util.Vector
import taboolib.module.nms.MinecraftVersion.versionId

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

// region NMSParticleImpl
class NMSParticleImpl : NMSParticle() {

    val version = versionId

    override fun createParticlePacket(particle: Particle, location: Location, offset: Vector, speed: Double, count: Int, data: Any?): Any {
        if (data != null && !particle.dataType.isInstance(data)) {
            error("data should be ${particle.dataType} (${data.javaClass})")
        }
        return if (MinecraftVersion.isHigher(MinecraftVersion.V1_12)) {
            val param = if (MinecraftVersion.isUnobfuscated) {
                CraftParticle.createParticleParam(particle, data)
            } else if (versionId >= 12002) {
                try {
                    org.bukkit.craftbukkit.v1_21_R3.CraftParticle.createParticleParam(particle, data)
                } catch (e: NoSuchMethodError) {
                    org.bukkit.craftbukkit.v1_16_R1.CraftParticle.toNMS(particle, data)
                }
            } else {
                org.bukkit.craftbukkit.v1_16_R1.CraftParticle.toNMS(particle, data)
            }
            if (MinecraftVersion.isUnobfuscated) {
                ClientboundLevelParticlesPacket(
                    param as ParticleOptions,
                    true,
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
            } else if (version > 12101) {
                net.minecraft.network.protocol.game.PacketPlayOutWorldParticles(
                    param as net.minecraft.core.particles.ParticleParam,
                    true,
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
                net.minecraft.server.v1_16_R1.PacketPlayOutWorldParticles(
                    param as net.minecraft.server.v1_16_R1.ParticleParam,
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
// endregion