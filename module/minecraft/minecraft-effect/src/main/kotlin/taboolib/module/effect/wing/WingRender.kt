package taboolib.module.effect.wing

import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.module.effect.ParticleSpawner
import taboolib.module.effect.coordinate.PlayerBackCoordinate
import kotlin.math.cos
import kotlin.math.sin

/**
 * 表示一个翅膀效果
 *
 * @author Soldier
 */
open class WingRender(var height: Int, var width: Int) {

    var size = 0

    val masks = HashMap<Char, ParticleSpawner>()

    val shape = Array(height + 1) { getSpace(width) }

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
     * 添加一行形状 每个char都对应一种特效
     */
    fun add(str: String) {
        add(str, size)
        size++
    }

    /**
     * 设置形状 每个char都对应一种特效
     */
    fun add(str: String, row: Int) {
        if (row >= height) {
            error("row${row} not match defined height($height)")
        }
        if (str.length != width) {
            error("string length(${str.length}) not match defined width($width)")
        }
        shape[row] = str
    }

    /**
     * 设置shape中某个字符对应的粒子效果
     */
    fun set(char: Char, particle: ParticleSpawner) {
        masks[char] = particle
    }

    fun set(char: Char, func: (Location) -> Unit) {
        masks[char] = object : ParticleSpawner {

            override fun spawn(location: Location) {
                func(location)
            }
        }
    }

    /**
     * 渲染一个翅膀
     * frame 接受从0-10的数字 对应翅膀的不同状态
     * 设置一个task 从0-10渲染 即可形成一个完整的翅膀粒子动画
     */
    open fun render(player: ProxyPlayer, frame: Int, x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) {
        val coordinate =
            PlayerBackCoordinate(player.location.add(x, y, z))
        if (frame % 5 == 0) {
            angle += offset
            if (angle % 30 == 0) {
                offset *= -1
            }
        }
        val yaw = Math.toRadians(angle.toDouble())
        for (wx in 0 until width) {
            for (hy in 0 until height) {
                val char = shape[height - 1 - hy][width - wx - 1]
                val mask = masks[char]
                if (mask != null) {
                    mask.spawn(coordinate.newLocation(wx * interval * cos(yaw), hy * interval, wx * -sin(yaw) * interval))
                    mask.spawn(coordinate.newLocation(-wx * interval * cos(yaw), hy * interval, wx * -sin(yaw) * interval))
                }
            }
        }
    }

    private fun getSpace(count: Int): String {
        val builder = StringBuilder()
        for (i in 0..count) {
            builder.append(" ")
        }
        return builder.toString()
    }
}