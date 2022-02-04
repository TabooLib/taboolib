package taboolib.module.effect

import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformTask
import taboolib.common.util.Location

/**
 * 表示一个特效对象
 *
 * @author Zoyn
 */
abstract class ParticleObj(var spawner: ParticleSpawner) {

    open lateinit var origin: Location
    open var period = 0L

    var showType = ShowType.NONE

    private var running = false
    private var matrix: Matrix? = null
    private var task: PlatformTask? = null

    fun addMatrix(matrix: Matrix) {
        if (this.matrix == null) {
            setMatrix(matrix)
        }
        this.matrix = matrix.multiply(this.matrix)
    }

    fun setMatrix(matrix: Matrix?) {
        this.matrix = matrix
    }

    fun removeMatrix() {
        matrix = null
    }

    fun hasMatrix(): Boolean {
        return matrix != null
    }

    abstract fun show()

    open fun alwaysShow() {
        turnOffTask()
        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        submit(delay = 2) {
            running = true
            task = submit(period = period) {
                if (running) {
                    show()
                }
            }
            showType = ShowType.ALWAYS_SHOW
        }
    }

    open fun alwaysShowAsync() {
        turnOffTask()
        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        submit(delay = 2) {
            running = true
            task = submit(period = period, async = true) {
                if (running) {
                    show()
                }
            }
            showType = ShowType.ALWAYS_SHOW_ASYNC
        }
    }

    open fun alwaysPlay() {
        if (this !is Playable) {
            try {
                throw NoSuchMethodException("The effect object is unplayable")
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
        }
        val playable = this as Playable
        turnOffTask()

        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        submit(delay = 2) {
            running = true
            task = submit(period = period) {
                if (running) {
                    playable.playNextPoint()
                }
            }
            showType = ShowType.ALWAYS_PLAY
        }
    }

    open fun alwaysPlayAsync() {
        if (this !is Playable) {
            try {
                throw NoSuchMethodException("The effect object is unplayable")
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
        }
        val playable = this as Playable
        turnOffTask()

        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        submit(delay = 2) {
            running = true
            task = submit(period = period, async = true) {
                if (running) {
                    playable.playNextPoint()
                }
            }
            showType = ShowType.ALWAYS_PLAY_ASYNC
        }
    }

    open fun turnOffTask() {
        if (task != null) {
            running = false
            task!!.cancel()
            showType = ShowType.NONE
        }
    }

    /**
     * 通过给定一个坐标就可以使用已经指定的参数来播放粒子
     * @param location 坐标
     */
    fun spawnParticle(location: Location) {
        var showLocation = location
        if (hasMatrix()) {
            val vector = location.clone().subtract(origin).toVector()
            val changed = matrix!!.applyVector(vector)
            showLocation = origin.clone().add(changed)
        }
        spawner.spawn(showLocation)
    }
}