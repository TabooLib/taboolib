package taboolib.module.effect

import taboolib.common.util.Location

/**
 * TabooLib
 * taboolib.module.effect.ParticleGenerator
 *
 * @author sky
 * @since 2021/6/30 11:43 下午
 */
interface ParticleSpawner {

    fun spawn(location: Location)
}