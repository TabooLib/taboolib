package taboolib.module.navigation

import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import taboolib.module.navigation.Fluid.Companion.getFluid
import taboolib.platform.util.callRegion
import kotlin.math.abs

/**
 * Navigation
 * ink.ptms.navigation.v2.NodeReader
 *
 * @author sky
 * @since 2021/2/21 11:57 下午
 */
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
        return getOrCreateNavigationNode(nodes, x, y, z)
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
        return entity.location.callRegion {
            getStartAtRegion()
        }
    }

    private fun getStartAtRegion(): Node {
        val position = Vector(0, 0, 0)
        val minHeight = world.navigationMinHeight()
        val maxHeight = world.maxHeight
        var y = entity.location.blockY.coerceIn(minHeight, maxHeight - 1)
        var block = world.getBlockAt(position.set(entity.location.blockX, y, entity.location.blockZ))
        var blockposition: Vector
        if (!entity.canStandOnFluid(block.getFluid())) {
            if (entity.canFloat && entity.isInWater()) {
                while (block.getFluid().isWater() && y < maxHeight - 1) {
                    ++y
                    block = world.getBlockAt(position.set(entity.location.blockX, y, entity.location.blockZ))
                }
                if (!block.getFluid().isWater()) {
                    --y
                }
            } else if (entity.isOnGround()) {
                y = NumberConversions.floor(entity.location.y + 0.5)
            } else {
                blockposition = entity.location.toVector().apply {
                    setY(blockY.coerceIn(minHeight, maxHeight - 1).toDouble())
                }
                var ground = blockposition.toBlock(block.world)
                while (!ground.type.isSolid && blockposition.blockY > minHeight) {
                    blockposition = blockposition.down()
                    ground = blockposition.toBlock(block.world)
                }
                y = if (ground.type.isSolid) blockposition.up().blockY.coerceAtMost(maxHeight - 1) else minHeight
            }
        } else {
            while (entity.canStandOnFluid(block.getFluid()) && y < maxHeight - 1) {
                ++y
                block = world.getBlockAt(position.set(entity.location.blockX, y, entity.location.blockZ))
            }
            if (!entity.canStandOnFluid(block.getFluid())) {
                --y
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
        return position.toLocation(world).callRegion {
            val block = position.toBlock(world)
            val blockHeight = NMS.instance.getBlockHeight(block)
            if (blockHeight == 0.0) 0.0 else blockHeight + block.y
        }
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
        return Vector(x, y, z).toLocation(world).callRegion {
            getLandNodeAtRegion(x, y, z)
        }
    }

    private fun getLandNodeAtRegion(x: Int, y: Int, z: Int): Node? {
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
        if (pathTypes == PathType.WALKABLE) {
            return node
        } else {
            // 方块类型为水，且实体无法浮在水上
            if (pathTypes == PathType.WATER && !entity.canFloat) {
                if (getCachedBlockType(x, h - 1, z) != PathType.WATER) {
                    return node
                }
                while (h > world.navigationMinHeight()) {
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
                    if (air < world.navigationMinHeight()) {
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
                node.isClosed = true
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
        return node.asBlockPos().toLocation(world).callRegion {
            isNeighborValidAtRegion(neighbor, node)
        }
    }

    private fun isNeighborValidAtRegion(neighbor: Node?, node: Node): Boolean {
        if (neighbor != null && !neighbor.isClosed && (neighbor.costMalus >= 0.0f || node.costMalus < 0.0f)) {
            val blockHeight = NMS.instance.getBlockHeight(node.asBlockPos().down().toBlock(world)) + node.y - 1
            val neighborHeight = NMS.instance.getBlockHeight(neighbor.asBlockPos().down().toBlock(world)) + neighbor.y - 1
            return abs(blockHeight - neighborHeight) < 1.25
        }
        return false
    }

    /**
     * 对角线合法
     */
    open fun isDiagonalValid(diagonal: Node, nodeLeft: Node?, nodeRight: Node?, node: Node?): Boolean {
        if (node == null || nodeRight == null || nodeLeft == null) {
            return false
        }
        if (node.isClosed) {
            return false
        }
        // 允许基数邻居比当前节点高至多 1 格（与 isNeighborValid 的高度容差保持一致）
        if (nodeRight.y > diagonal.y + 1 || nodeLeft.y > diagonal.y + 1) {
            return false
        }
        if (nodeLeft.type == PathType.WALKABLE_DOOR || nodeRight.type == PathType.WALKABLE_DOOR || node.type == PathType.WALKABLE_DOOR) {
            return false
        }
        val flag = nodeRight.type == PathType.FENCE && nodeLeft.type == PathType.FENCE && entity.width < 0.5
        return node.costMalus >= 0.0f
                && (nodeRight.y < diagonal.y || nodeRight.costMalus >= 0.0f || flag)
                && (nodeLeft.y < diagonal.y || nodeLeft.costMalus >= 0.0f || flag)
    }

    open fun getNeighbors(nodes: Array<Node?>, node: Node): Int {
        return node.asBlockPos().toLocation(world).callRegion {
            getNeighborsAtRegion(nodes, node)
        }
    }

    private fun getNeighborsAtRegion(nodes: Array<Node?>, node: Node): Int {
        var neighbors = 0
        // 北
        val north = getLandNode(node.x, node.y, node.z - 1)
        if (isNeighborValid(north, node)) {
            nodes[neighbors++] = north
        }
        // 西
        val west = getLandNode(node.x - 1, node.y, node.z)
        if (isNeighborValid(west, node)) {
            nodes[neighbors++] = west
        }
        // 南
        val south = getLandNode(node.x, node.y, node.z + 1)
        if (isNeighborValid(south, node)) {
            nodes[neighbors++] = south
        }
        // 东
        val east = getLandNode(node.x + 1, node.y, node.z)
        if (isNeighborValid(east, node)) {
            nodes[neighbors++] = east
        }
        // 西北
        val westNorth = getLandNode(node.x - 1, node.y, node.z - 1)
        if (isDiagonalValid(node, west, north, westNorth)) {
            nodes[neighbors++] = westNorth
        }
        // 西南
        val westSouth = getLandNode(node.x - 1, node.y, node.z + 1)
        if (isDiagonalValid(node, west, south, westSouth)) {
            nodes[neighbors++] = westSouth
        }
        // 东北
        val eastNorth = getLandNode(node.x + 1, node.y, node.z - 1)
        if (isDiagonalValid(node, east, north, eastNorth)) {
            nodes[neighbors++] = eastNorth
        }
        // 东南
        val eastSouth = getLandNode(node.x + 1, node.y, node.z + 1)
        if (isDiagonalValid(node, east, south, eastSouth)) {
            nodes[neighbors++] = eastSouth
        }
        return neighbors
    }
}

@JvmSynthetic
internal fun getOrCreateNavigationNode(nodes: MutableMap<Int, Node>, x: Int, y: Int, z: Int): Node {
    val initialKey = Node.createHash(x, y, z)
    var key = initialKey
    while (true) {
        val existing = nodes[key]
        if (existing == null) {
            return Node(x, y, z).also { nodes[key] = it }
        }
        if (existing.x == x && existing.y == y && existing.z == z) {
            return existing
        }
        key = key * 31 + 1
        check(key != initialKey) { "Unable to resolve navigation node hash collision" }
    }
}
