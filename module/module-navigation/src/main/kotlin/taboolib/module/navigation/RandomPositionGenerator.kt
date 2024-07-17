package taboolib.module.navigation

import org.bukkit.util.Vector
import java.util.*
import kotlin.math.*

object RandomPositionGenerator {

    private const val PI_OF_TWO = 1.5707963705062866
    private const val SQRT_OF_TWO = 1.4142135623730951

    /**
     * 生成地面随机坐标
     *
     * @param nodeEntity 代理实体
     * @param restrictX X轴约束
     * @param restrictY Y轴约束
     */
    fun generateLand(nodeEntity: NodeEntity, restrictX: Int, restrictY: Int): Vector? {
        return generate(nodeEntity, restrictX, restrictY, null, onWater = true, aboveLand = false, requireWalkable = true)
    }

    /**
     * 生成地面随机坐标
     *
     * @param nodeEntity 代理实体
     * @param restrictX X轴约束
     * @param restrictY Y轴约束
     */
    fun generateLandAbove(nodeEntity: NodeEntity, restrictX: Int, restrictY: Int): Vector? {
        return generate(nodeEntity, restrictX, restrictY, null, onWater = false, aboveLand = true, requireWalkable = true)
    }

    /**
     * 生成地面随机坐标
     *
     * @param nodeEntity 代理实体
     * @param restrictX X轴约束
     * @param restrictY Y轴约束
     * @param start 起点
     */
    fun generateLandAbove(nodeEntity: NodeEntity, restrictX: Int, restrictY: Int, start: Vector): Vector? {
        return generate(nodeEntity, restrictX, restrictY, start, onWater = false, aboveLand = true, requireWalkable = true)
    }


    /**
     * 生成朝向目标的地面随机坐标
     *
     * @param nodeEntity 代理实体
     * @param restrictX X轴约束
     * @param restrictY Y轴约束
     * @param target 目标
     */
    fun generateLandTowards(nodeEntity: NodeEntity, restrictX: Int, restrictY: Int, target: Vector): Vector? {
        val towards = target.subtract(Vector(nodeEntity.x, nodeEntity.y, nodeEntity.z))
        return generate(nodeEntity, restrictX, restrictY, towards, onWater = false, aboveLand = true, requireWalkable = true)
    }

    /**
     * 生成朝向目标的空中随机坐标
     *
     * @param nodeEntity 代理实体
     * @param restrictX X轴约束
     * @param restrictY Y轴约束
     * @param target 目标
     */
    fun generateAirTowards(nodeEntity: NodeEntity, restrictX: Int, restrictY: Int, target: Vector): Vector? {
        val towards = target.subtract(Vector(nodeEntity.x, nodeEntity.y, nodeEntity.z))
        return generate(nodeEntity, restrictX, restrictY, towards, onWater = false, aboveLand = true, requireWalkable = false)
    }

    /**
     * 生成远离起点的地面随机坐标
     *
     * @param nodeEntity 代理实体
     * @param restrictX X轴约束
     * @param restrictY Y轴约束
     * @param start 起点
     */
    fun generateLandAvoid(nodeEntity: NodeEntity, restrictX: Int, restrictY: Int, start: Vector): Vector? {
        val avoid = nodeEntity.location.subtract(start).toVector()
        return generate(nodeEntity, restrictX, restrictY, avoid, onWater = false, aboveLand = true, requireWalkable = true)
    }

    /**
     * 生成随机坐标
     *
     * @param nodeEntity 代理实体
     * @param rX X轴约束
     * @param yY Y轴约束
     * @param start 起点
     * @param onWater 允许水上
     * @param aboveLand 计算地面之上的空间
     * @param requireWalkable 依赖可行走的方块
     */
    fun generate(nodeEntity: NodeEntity, rX: Int, yY: Int, start: Vector?, onWater: Boolean, aboveLand: Boolean, requireWalkable: Boolean): Vector? {
        val random: Random = nodeEntity.random
        val navigation = PathTypeFactory(nodeEntity)
        val hasRestriction: Boolean = if (nodeEntity.hasRestriction) {
            nodeEntity.restrictCenter.closerThan(nodeEntity.location.toCommonVector(), (nodeEntity.restrictRadius + rX.toFloat()).toDouble() + 1.0)
        } else {
            false
        }
        var find = false
        var stop = false
        var sort = Double.NEGATIVE_INFINITY
        var position = nodeEntity.location.toCommonVector()
        repeat(10) {
            if (stop) {
                return@repeat
            }
            val delta = randomDelta(random, rX, yY, start) ?: return@repeat
            var x = delta.x
            val y = delta.y
            var z = delta.z
            if (nodeEntity.hasRestriction && rX > 1) {
                if (nodeEntity.x > nodeEntity.restrictCenter.x) {
                    x -= random.nextInt(rX / 2)
                } else {
                    x += random.nextInt(rX / 2)
                }
                if (nodeEntity.z > nodeEntity.restrictCenter.z) {
                    z -= random.nextInt(rX / 2)
                } else {
                    z += random.nextInt(rX / 2)
                }
            }
            var result = Vector((x + nodeEntity.x).toInt(), (y + nodeEntity.y).toInt(), (z + nodeEntity.z).toInt())
            if (result.y < 0) {
                return@repeat
            }
            if (hasRestriction && !nodeEntity.isWithinRestriction(result)) {
                return@repeat
            }
            if (requireWalkable && navigation.getTypeAsWalkable(nodeEntity.location.world!!, result) != PathType.WALKABLE) {
                return@repeat
            }
            if (aboveLand) {
                result = moveUp(result, 0, 256) {
                    nodeEntity.location.world!!.getBlockAt(it.toLocation(nodeEntity.location.world!!)).type.isSolid
                }
            }
            if (onWater || nodeEntity.location.world!!.getBlockAt(result.toLocation(nodeEntity.location.world!!)).type.isWater()) {
                val type = navigation.getTypeAsWalkable(nodeEntity.location.world!!, result)
                if (nodeEntity.getPathfindingMalus(type) == 0.0f) {
                    val walk = nodeEntity.getWalkTargetValue(result)
                    if (walk > sort) {
                        find = true
                        sort = walk
                        stop = walk < 0
                        position = result
                    }
                }
            }
        }
        return if (find) position.bottomCenter() else null
    }

    /**
     * 通过条件不断向上移动坐标指针
     */
    fun moveUp(position: Vector, y: Int, maxY: Int, check: (Vector) -> Boolean): Vector {
        return if (y < 0) {
            throw IllegalArgumentException("y < 0")
        } else if (!check(position)) {
            position
        } else {
            var pos = position.up()
            while (pos.y < maxY && check(pos)) {
                pos = pos.up()
            }
            var result = pos
            while (result.y < maxY && result.y - pos.y < y) {
                val temp = result.up()
                if (check(temp)) {
                    break
                }
                result = temp
            }
            result
        }
    }

    private fun randomDelta(random: Random, restrictX: Int, restrictY: Int, vector: Vector?): Vector? {
        return if (vector != null) {
            val size = atan2(vector.z, vector.x) - PI_OF_TWO
            val delta = size + (2.0f * random.nextFloat() - 1.0f).toDouble() * PI_OF_TWO
            val range = sqrt(random.nextDouble()) * SQRT_OF_TWO * restrictX.toDouble()
            val rX = -range * sin(delta)
            val aX = range * cos(delta)
            if (abs(rX) <= restrictX.toDouble() && abs(aX) <= restrictX.toDouble()) {
                Vector(rX.toInt(), random.nextInt(2 * restrictY + 1) - restrictY, aX.toInt())
            } else {
                null
            }
        } else {
            val rX = random.nextInt(2 * restrictX + 1) - restrictX
            val rY = random.nextInt(2 * restrictY + 1) - restrictY
            val rZ = random.nextInt(2 * restrictX + 1) - restrictX
            Vector(rX, rY, rZ)
        }
    }
}