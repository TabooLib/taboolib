@file:Isolated

package taboolib.module.effect

import taboolib.common.Isolated
import taboolib.common.util.Location
import taboolib.module.effect.renderer.GeneralEquationRenderer
import taboolib.module.effect.renderer.ParametricEquationRenderer
import taboolib.module.effect.renderer.PolarEquationRenderer

/**
 * 创建一个普通方程渲染器
 *
 * @param origin 原点
 * @param function 函数
 * @param minX 最小X
 * @param maxX 最大X
 * @param dx 每次增加的X
 * @param period 特效周期(如果需要可以使用)
 */
fun createGeneralEquationRenderer(
    origin: Location,
    function: (x: Double) -> Double,
    minX: Double = -5.0,
    maxX: Double = 5.0,
    dx: Double = 0.1,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): GeneralEquationRenderer {
    return GeneralEquationRenderer(origin, function, minX, maxX, dx, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一个参数方程渲染器
 *
 * @param origin 原点
 * @param xFunction X函数
 * @param yFunction Y函数
 * @param zFunction Z函数
 * @param minT 最小T
 * @param maxT 最大T
 * @param dt 每次增加的T
 * @param period 特效周期(如果需要可以使用)
 */
fun createParametricEquationRenderer(
    origin: Location,
    xFunction: (x: Double) -> Double,
    yFunction: (y: Double) -> Double,
    zFunction: (z: Double) -> Double = { 0.0 },
    minT: Double = -5.0,
    maxT: Double = 5.0,
    dt: Double = 0.1,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): ParametricEquationRenderer {
    return ParametricEquationRenderer(origin, xFunction, yFunction, zFunction, minT, maxT, dt, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一个极坐标方程渲染器
 *
 * @param origin 原点
 * @param rFunction R函数
 * @param minT 最小T
 * @param maxT 最大T
 * @param dt 每次增加的T
 * @param period 特效周期(如果需要可以使用)
 */
fun createPolarEquationRenderer(
    origin: Location,
    rFunction: (r: Double) -> Double,
    minT: Double = -5.0,
    maxT: Double = 5.0,
    dt: Double = 0.1,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): PolarEquationRenderer {
    return PolarEquationRenderer(origin, rFunction, minT, maxT, dt, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}