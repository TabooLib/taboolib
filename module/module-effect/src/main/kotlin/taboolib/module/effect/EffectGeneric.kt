@file:Isolated
@file:Suppress("SpellCheckingInspection")

package taboolib.module.effect

import taboolib.common.Isolated
import taboolib.common.util.Location

/**
 * 创建一个弧
 *
 * @param origin 弧的中心
 * @param startAngle 弧的起始角度
 * @param angle 弧的角度
 * @param radius 弧的半径
 * @param step 弧的步长
 * @param period 特效周期(如果需要可以使用)
 */
fun createArc(
    origin: Location,
    startAngle: Double = 0.0,
    angle: Double = 30.0,
    radius: Double = 1.0,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit
): Arc {
    return Arc(origin, startAngle, angle, radius, step, period, object : ParticleSpawner {

        override fun spawn(location: Location) {
            spawner(location)
        }
    })
}

/**
 * 创建一个星型线
 *
 * @param origin 中心点
 * @param radius 半径
 * @param step 步长
 * @param period 特效周期(如果需要可以使用)
 */
fun createAstroid(
    origin: Location,
    radius: Double = 1.0,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit
): Astroid {
    return Astroid(radius, origin, object : ParticleSpawner {

        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also {
        it.step = step
        it.period = period
    }
}

/**
 * 创建一个圆
 *
 * @param origin 圆心
 * @param radius 半径
 * @param step 每个粒子的间隔(也即步长)
 * @param period 特效周期(如果需要可以使用)
 */
fun createCircle(
    origin: Location,
    radius: Double = 1.0,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit
): Circle {
    return Circle(origin, radius, step, period, object : ParticleSpawner {

        override fun spawn(location: Location) {
            spawner(location)
        }
    })
}

/**
 * 创建实心圆
 *
 * @param origin 圆心
 * @param radius 半径
 * @param sample 粒子数量
 * @param period 特效周期(如果需要可以使用)
 */
fun createFilledCircle(
    origin: Location,
    radius: Double = 1.0,
    sample: Int = 100,
    period: Long = 20,
    spawner: (p: Location) -> Unit
): FilledCircle {
    return FilledCircle(origin, radius, sample, object : ParticleSpawner {

        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一个立方体
 *
 * @param min 最小点
 * @param max 最大点
 * @param step 每个粒子的间隔(也即步长)
 * @param period 特效周期(如果需要可以使用)
 */
fun createCube(
    min: Location,
    max: Location,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit
): Cube {
    return Cube(min, max, step, object : ParticleSpawner {

        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一条线
 *
 * @param start 起始点
 * @param end 结束点
 * @param step 每个粒子的间隔(也即步长)
 * @param period 特效周期(如果需要可以使用)
 */
fun createLine(
    start: Location,
    end: Location,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit
): Line {
    return Line(start, end, step, period, object : ParticleSpawner {

        override fun spawn(location: Location) {
            spawner(location)
        }
    })
}

/**
 * 创建一个正多边形
 *
 * @param radius 半径
 * @param sides 边数
 * @param step 每个粒子的间隔(也即步长)
 * @param period 特效周期(如果需要可以使用)
 */
fun createPolygon(
    origin: Location,
    radius: Double = 1.0,
    sides: Int = 3,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit
): Polygon {
    return Polygon(sides, origin, step, object : ParticleSpawner {

        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also {
        it.radius = radius
        it.period = period
    }
}

/**
 * 创建一个球
 *
 * @param origin 球心
 * @param radius 半径
 * @param sample 粒子数量
 * @param period 特效周期(如果需要可以使用)
 */
fun createSphere(
    origin: Location,
    radius: Double = 1.0,
    sample: Int = 100,
    period: Long = 20,
    spawner: (p: Location) -> Unit
): Sphere {
    return Sphere(origin, sample, radius, object : ParticleSpawner {

        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}