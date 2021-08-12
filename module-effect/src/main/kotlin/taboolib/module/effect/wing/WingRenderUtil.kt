package taboolib.module.effect.wing

import taboolib.common.platform.ProxyPlayer
import taboolib.module.effect.PlayerBackCoordinate
import kotlin.math.cos
import kotlin.math.sin

/**
 * 表示一个翅膀效果
 *
 * @author Soldier
 */
class WingRenderUtil(var height: Int, var width: Int) {

    val shape: Array<Array<Char>> = Array(height + 1) { Array(width) { ' ' } }

    var i = 0

    /**
     * 添加一行形状
     */
    fun addShape(array: Array<Char>) {
        shape[i] = array
        i++
    }

    /**
     * 设置形状 每个char都对应一种特效
     */
    fun setShape(array: Array<Char>, row: Int) {
        shape[row] = array
    }

    val masks: MutableMap<Char, WingParticle> = HashMap()

    /**
     * 设置shape中某个字符对应的粒子效果
     */
    fun setMask(char: Char, particle: WingParticle) {
        masks[char] = particle
    }

    /**
     * 每个粒子间的间隔
     */
    val interval: Double = 0.2

    /**
     * 翅膀当前打开的角度 一般情况下不需要修改
     */
    var angle = 0

    /**
     * 翅膀煽动旋转的角度
     */
    var offset = 5

    /**
     * 渲染一个翅膀
     * frame 接受从0-10的数字 对应翅膀的不同状态
     * 设置一个task 从0-10渲染 即可形成一个完整的翅膀粒子动画
     */
    fun render(player: ProxyPlayer, frame: Int, x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) {
        val coordinate = PlayerBackCoordinate(player.location.add(x, y, z))
        if (frame % 5 == 0) {
            angle += offset
            if (angle % 30 == 0) {
                offset *= -1
            }
        }
        val yaw = Math.toRadians(angle.toDouble())
        for (x in 0 until width) {
            for (y in 0 until height) {
                val char = shape[height - 1 - y][width - x - 1]
                val mask = masks[char]
                if (mask != null) {
                    mask.particle.sendTo(
                        coordinate.newLocation(
                            x * interval * cos(yaw),
                            y * interval,
                            x * -sin(yaw) * interval
                        ), 128.0, mask.getOffset(), 0
                    )
                    mask.particle.sendTo(
                        coordinate.newLocation(
                            -x * interval * cos(yaw),
                            y * interval,
                            x * -sin(yaw) * interval
                        ), 128.0, mask.getOffset(), 0
                    )
                }
            }
        }
    }
}