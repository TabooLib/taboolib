package taboolib.module.effect.wing.impl

import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.module.effect.wing.WingParticle
import taboolib.module.effect.wing.WingRenderUtil

/**
 * 使用粒子翅膀工具制作的一个翅膀动画demo
 *
 * @author Soldier
 */
class AngelWing {
    /**
     * 这里需要指定该特效的height和width
     */
    val wingRenderer = WingRenderUtil(7, 5)

    init {
        wingRenderer.addShape(" a   ")
        wingRenderer.addShape(" aa  ")
        wingRenderer.addShape(" aaa ")
        wingRenderer.addShape("  aaa")
        wingRenderer.addShape("  aaa")
        wingRenderer.addShape(" aaaa")
        wingRenderer.addShape(" aaa ")
        wingRenderer.addShape(" a   ")
        wingRenderer.setMask('a', WingParticle(ProxyParticle.REDSTONE, 255, 255, 255))
//      这里指定了a这个字符为白色粒子。 如果需要使用rgb颜色，务必使用REDSTONE效果
    }

    /**
     * 从0-10传入frame，即可渲染出一个完整的翅膀粒子动画
     */
    fun render(player: ProxyPlayer, frame: Int) {
        wingRenderer.render(player, frame, 0.0, 0.5, 0.0)
    }
}