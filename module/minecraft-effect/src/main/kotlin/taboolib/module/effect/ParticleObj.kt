package taboolib.module.effect

import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor.PlatformTask
import taboolib.common.util.Location
import taboolib.module.effect.math.Matrix

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

    private var incrementX = 0.0
    private var incrementY = 0.0
    private var incrementZ = 0.0

    open fun getIncrementX(): Double {
        return incrementX
    }

    open fun getIncrementY(): Double {
        return incrementY
    }

    open fun getIncrementZ(): Double {
        return incrementZ
    }

    open fun setIncrementX(incrementX: Double): ParticleObj {
        this.incrementX = incrementX
        return this
    }

    open fun setIncrementY(incrementY: Double): ParticleObj {
        this.incrementY = incrementY
        return this
    }

    open fun setIncrementZ(incrementZ: Double): ParticleObj {
        this.incrementZ = incrementZ
        return this
    }

    /**
     * 得到该特效对象的矩阵
     *
     * @return [Matrix]
     */
    open fun getMatrix(): Matrix? {
        return matrix
    }

    /**
     * 给该特效对象叠加一个矩阵
     *
     * @param matrix 给定的矩阵
     * @return [ParticleObj]
     */
    open fun addMatrix(matrix: Matrix): ParticleObj {
        if (this.matrix == null) {
            setMatrix(matrix)
            return this
        }
        this.matrix = matrix.multiply(this.matrix)
        return this
    }

    /**
     * 给该特效对象设置一个矩阵
     * 该方法将会直接覆盖之前所有已经变换好的矩阵
     *
     * @param matrix 给定的矩阵
     * @return [ParticleObj]
     */
    open fun setMatrix(matrix: Matrix?): ParticleObj {
        this.matrix = matrix
        return this
    }
    /**
     * 移除该特效对象的矩阵
     * @return [ParticleObj]
     */
    open fun removeMatrix(): ParticleObj {
        matrix = null
        return this
    }

    fun hasMatrix(): Boolean {
        return matrix != null
    }

    abstract fun show()

    abstract fun calculateLocations(): List<Location>

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

        // 在这里可以设置一个XYZ的变化量
        showLocation.add(incrementX, incrementY, incrementZ)

        spawner.spawn(showLocation)
    }
}