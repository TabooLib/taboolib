package taboolib.module.navigation

import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import taboolib.module.navigation.Fluid.Companion.getFluid

/**
 * Navigation
 * ink.ptms.navigation.v2.NodeReader
 *
 * @author sky
 * @since 2021/2/21 11:57 下午
 */
@Suppress("LiftReturnOrAssignment")
open class NodeReader(val entity: NodeEntity) {

    val nodes = HashMap<Int, Node>()
    val type = HashMap<Long, PathType>()
    val typeGetter = PathTypeFactory(entity)
    val world = entity.location.world!!

    open fun done() {
        nodes.clear()
        type.clear()
    }

    open fun getGoal(x: Double, y: Double, z: Double): NodeTarget {
        return NodeTarget(getNode(NumberConversions.floor(x), NumberConversions.floor(y), NumberConversions.floor(z)))
    }

    fun getNode(position: Vector): Node {
        return getNode(position.blockX, position.blockY, position.blockZ)
    }

    fun getNode(x: Int, y: Int, z: Int): Node {
        return nodes.computeIfAbsent(Node.createHash(x, y, z)) { Node(x, y, z) }
    }

    fun getCachedBlockType(x: Int, y: Int, z: Int): PathType {
        return getCachedBlockType(Vector(x, y, z))
    }

    fun getCachedBlockType(position: Vector): PathType {
        return type.computeIfAbsent(position.hash()) {
            typeGetter.getTypeAsBoundingBox(position.blockX, position.blockY, position.blockZ)
        }
    }

    /**
     * 获取起点
     */
    fun getStart(): Node {
        val position = Vector(0, 0, 0)
        var y = entity.location.blockY
        var block = world.getBlockAt(position.set(entity.location.blockX, y, entity.location.blockZ))
        var blockposition: Vector
        if (!entity.canStandOnFluid(block.getFluid())) {
            if (entity.canFloat && entity.isInWater()) {
                while (true) {
                    if (!block.isLiquid) {
                        --y
                        break
                    }
                    ++y
                    block = world.getBlockAt(position.set(entity.location.blockX, y, entity.location.blockZ))
                }
            } else if (entity.isOnGround()) {
                y = NumberConversions.floor(entity.location.y + 0.5)
            } else {
                blockposition = entity.location.toVector()
                while (!blockposition.toBlock(block.world).type.isSolid && blockposition.y > 0) {
                    blockposition = blockposition.down()
                }
                y = blockposition.up().blockY
            }
        } else {
            while (true) {
                if (!entity.canStandOnFluid(block.getFluid())) {
                    --y
                    break
                }
                ++y
                block = world.getBlockAt(position.set(entity.location.blockX, y, entity.location.blockZ))
            }
        }
        blockposition = entity.location.toVector()
        val blockPathType = getCachedBlockType(blockposition.blockX, y, blockposition.blockZ)
        if (entity.getPathfindingMalus(blockPathType) < 0.0f) {
            val boundingBox = entity.boundingBox
            if (hasPositiveMalus(position.set(boundingBox.minX, y.toDouble(), boundingBox.minZ))
                || hasPositiveMalus(position.set(boundingBox.minX, y.toDouble(), boundingBox.maxZ))
                || hasPositiveMalus(position.set(boundingBox.maxX, y.toDouble(), boundingBox.minZ))
                || hasPositiveMalus(position.set(boundingBox.maxX, y.toDouble(), boundingBox.maxZ))
            ) {
                val node = getNode(position)
                node.type = getCachedBlockType(node.asBlockPos())
                node.costMalus = entity.getPathfindingMalus(node.type)
                return node
            }
        }
        val node = getNode(blockposition.blockX, y, blockposition.blockZ)
        node.type = getCachedBlockType(node.asBlockPos())
        node.costMalus = entity.getPathfindingMalus(node.type)
        return node
    }

    /**
     * 获取当前坐标下的陆地高度
     * 指最顶层碰撞箱 maxY > 0 的方块
     */
    fun getLandHeight(position: Vector): Double {
        val block = position.toBlock(world)
        val blockHeight = NMS.INSTANCE.getBlockHeight(block)
        return if (blockHeight == 0.0) 0.0 else blockHeight + block.y
    }

    /**
     * 获取陆地节点
     * 砍掉三个参数
     * l: Int
     * direction: Direction
     * blockPathType: BlockPathTypes
     *
     * @param x x
     * @param y y
     * @param z z
     */
    open fun getLandNode(x: Int, y: Int, z: Int): Node? {
        var h = y
        var node: Node? = null
        // 获取方块类型
        var pathTypes = getCachedBlockType(x, h, z)
        // 如果被阻挡则尝试越过
        if (pathTypes == PathType.BLOCKED) {
            pathTypes = getCachedBlockType(x, ++h, z)
        }
        // 实体是否可以通过该方块
        var malusByEntity = entity.getPathfindingMalus(pathTypes)
        if (malusByEntity >= 0) {
            node = getNode(x, h, z)
            node.type = pathTypes
            node.costMalus = node.costMalus.coerceAtLeast(malusByEntity)
        }
//        if (blockPathType == BlockPathTypes.FENCE && node != null && node.cost >= 0.0f && !canReachWithoutCollision(node)) {
//            node = null
//        }
        if (pathTypes == PathType.WALKABLE) {
            return node
        } else {
//            val width = entity.width / 2
//            if ((node == null || node.costMalus < 0.0f) && l > 0 && pathTypes != BlockPathTypes.FENCE && pathTypes != BlockPathTypes.UNPASSABLE_RAIL && pathTypes != BlockPathTypes.TRAPDOOR) {
//                node = getLand(x, h + 1, z, l - 1, fromLandHeight, direction, blockPathType)
//                if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && entity.width < 1.0f) {
//                    val d3 = (x - direction.getAdjacentX()).toDouble() + 0.5
//                    val d4 = (z - direction.getAdjacentZ()).toDouble() + 0.5
//                    val boundingBox = BoundingBox(
//                        d3 - width,
//                        getLandHeight(world!!, position.set(d3, (h + 1).toDouble(), d4)) + 0.001,
//                        d4 - width,
//                        d3 + width,
//                        entity.height + getLandHeight(world!!, position.set(node.x.toDouble(), node.y.toDouble(), node.z.toDouble())) - 0.002,
//                        d4 + width
//                    )
//                    if (hasCollisions(boundingBox)) {
//                        node = null
//                    }
//                }
//            }
            // 方块类型为水，且实体无法浮在水上
            if (pathTypes == PathType.WATER && !entity.canFloat) {
                if (getCachedBlockType(x, h - 1, z) != PathType.WATER) {
                    return node
                }
                while (h > 0) {
                    --h
                    pathTypes = getCachedBlockType(x, h, z)
                    if (pathTypes != PathType.WATER) {
                        return node
                    }
                    node = getNode(x, h, z)
                    node.type = pathTypes
                    node.costMalus = node.costMalus.coerceAtLeast(entity.getPathfindingMalus(pathTypes))
                }
            }
            // 方块可以通过
            if (pathTypes == PathType.OPEN) {
                var airSupply = 0
                var air = h
                while (pathTypes == PathType.OPEN) {
                    --air
                    if (air < 0) {
                        val node1 = getNode(x, air, z)
                        node1.type = PathType.BLOCKED
                        node1.costMalus = -1.0f
                        return node1
                    }
                    if (airSupply++ >= entity.getAirSupply()) {
                        val node1 = getNode(x, air, z)
                        node1.type = PathType.BLOCKED
                        node1.costMalus = -1.0f
                        return node1
                    }
                    pathTypes = getCachedBlockType(x, air, z)
                    malusByEntity = entity.getPathfindingMalus(pathTypes)
                    if (pathTypes != PathType.OPEN && malusByEntity >= 0.0f) {
                        node = getNode(x, air, z)
                        node.type = pathTypes
                        node.costMalus = node.costMalus.coerceAtLeast(malusByEntity)
                        break
                    }
                    if (malusByEntity < 0.0f) {
                        node = getNode(x, air, z)
                        node.type = PathType.BLOCKED
                        node.costMalus = -1.0f
                        return node
                    }
                }
            }
            if (pathTypes == PathType.FENCE) {
                node = getNode(x, h, z)
                node.closed = true
                node.type = pathTypes
                node.costMalus = pathTypes.malus
            }
            return node
        }
    }

    /**
     * 实体允许通过该方块
     */
    fun hasPositiveMalus(position: Vector): Boolean {
        return entity.getPathfindingMalus(getCachedBlockType(position)) >= 0.0f
    }

    /**
     * 临近合法
     */
    open fun isNeighborValid(neighbor: Node?, node: Node): Boolean {
        return neighbor != null && !neighbor.closed && (neighbor.costMalus >= 0.0f || node.costMalus < 0.0f)
    }

    /**
     * 对角线合法
     */
    open fun isDiagonalValid(diagonal: Node, nodeLeft: Node?, nodeRight: Node?, node: Node?): Boolean {
        return if (node != null && nodeRight != null && nodeLeft != null) {
            if (node.closed) {
                false
            } else if (nodeRight.y <= diagonal.y && nodeLeft.y <= diagonal.y) {
                if (nodeLeft.type != PathType.WALKABLE_DOOR && nodeRight.type != PathType.WALKABLE_DOOR && node.type != PathType.WALKABLE_DOOR) {
                    val flag = nodeRight.type == PathType.FENCE && nodeLeft.type == PathType.FENCE && entity.width < 0.5
                    node.costMalus >= 0.0f
                            && (nodeRight.y < diagonal.y || nodeRight.costMalus >= 0.0f || flag)
                            && (nodeLeft.y < diagonal.y || nodeLeft.costMalus >= 0.0f || flag)
                } else {
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
    }

    open fun getNeighbors(nodes: Array<Node?>, node: Node): Int {
        var neighbors = 0
        var j = 0
//        val pathType0 = getCachedBlockType(node.x, node.y + 1, node.z)
//        val pathType1 = getCachedBlockType(node.x, node.y, node.z)
//        if (entity.getPathfindingMalus(pathType0) >= 0.0f && pathType1 != BlockPathTypes.STICKY_HONEY) {
//            j = d(entity.G.coerceAtMost(1f))
//        }
        // 北
        val north = getLandNode(node.x, node.y, node.z - 1)
        if (isNeighborValid(north, node)) {
            nodes[neighbors++] = north
        }
        // 东
        val west = getLandNode(node.x - 1, node.y, node.z)
        if (isNeighborValid(west, node)) {
            nodes[neighbors++] = west
        }
        // 南
        val south = getLandNode(node.x, node.y, node.z + 1)
        if (isNeighborValid(south, node)) {
            nodes[neighbors++] = south
        }
        // 西
        val east = getLandNode(node.x + 1, node.y, node.z)
        if (isNeighborValid(east, node)) {
            nodes[neighbors++] = east
        }
        // 东北
        val westNorth = getLandNode(node.x - 1, node.y, node.z - 1)
        if (isDiagonalValid(node, west, north, westNorth)) {
            nodes[neighbors++] = westNorth
        }
        // 东南
        val westSouth = getLandNode(node.x - 1, node.y, node.z + 1)
        if (isDiagonalValid(node, west, south, westSouth)) {
            nodes[neighbors++] = westSouth
        }
        // 西南
        val eastSouth = getLandNode(node.x + 1, node.y, node.z - 1)
        if (isDiagonalValid(node, east, north, eastSouth)) {
            nodes[neighbors++] = eastSouth
        }
        // 西北
        val southNorth = getLandNode(node.x + 1, node.y, node.z + 1)
        if (isDiagonalValid(node, east, south, southNorth)) {
            nodes[neighbors++] = southNorth
        }
        return neighbors
    }
}