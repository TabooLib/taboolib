package taboolib.module.navigation

/**
 * Navigation
 * taboolib.module.navigation.BinaryHeap
 *
 * @author sky
 * @since 2021/2/21 9:48 下午
 */
class BinaryHeap {

    var heap = arrayOfNulls<Node>(128)
    var size = 0

    /**
     * a
     */
    fun insert(node: Node): Node {
        return if (node.heapIdx >= 0) {
            throw IllegalStateException("OW KNOWS!")
        } else {
            if (size == heap.size) {
                val newHeap = arrayOfNulls<Node>(size shl 1)
                System.arraycopy(heap, 0, newHeap, 0, size)
                heap = newHeap
            }
            heap[size] = node
            node.heapIdx = size
            upHeap(size++)
            node
        }
    }

    /**
     * a
     */
    fun clear() {
        size = 0
    }

    /**
     * c
     */
    fun pop(): Node {
        val pop = heap[0]
        heap[0] = heap[--size]
        heap[size] = null
        if (size > 0) {
            downHeap(0)
        }
        pop!!.heapIdx = -1
        return pop
    }

    /**
     * a
     */
    fun changeCost(node: Node, value: Float) {
        val cost = node.cost
        node.cost = value
        if (value < cost) {
            upHeap(node.heapIdx)
        } else {
            downHeap(node.heapIdx)
        }
    }

    /**
     * a
     */
    fun upHeap(value: Int) {
        var currentIndex = value
        val currentNode = heap[currentIndex]
        val currentCost = currentNode!!.actualCost
        while (currentIndex > 0) {
            val parentIndex = (currentIndex - 1) shr 1
            val parentNode = heap[parentIndex]
            if (currentCost >= parentNode!!.actualCost) {
                break
            }
            heap[currentIndex] = parentNode
            parentNode.heapIdx = currentIndex
            currentIndex = parentIndex
        }
        heap[currentIndex] = currentNode
        currentNode.heapIdx = currentIndex
    }

    /**
     * b
     */
    fun downHeap(value: Int) {
        var currentIndex = value
        val currentNode = heap[currentIndex]!!
        val currentCost = currentNode.cost
        while (true) {
            val leftChildIndex = 1 + (currentIndex shl 1)
            val rightChildIndex = leftChildIndex + 1
            // 检查左子节点索引是否超出范围
            if (leftChildIndex >= size) {
                break
            }
            val leftChildNode = heap[leftChildIndex]!!
            val leftChildCost = leftChildNode.cost
            // 确定右子节点的节点和成本
            val (rightChildNode, rightChildCost) = if (rightChildIndex >= size) {
                null to Float.POSITIVE_INFINITY
            } else {
                heap[rightChildIndex]!! to heap[rightChildIndex]!!.cost
            }
            // 比较左右子节点的成本
            if (leftChildCost < rightChildCost) {
                if (leftChildCost >= currentCost) {
                    break
                }
                // 将左子节点上移
                heap[currentIndex] = leftChildNode
                leftChildNode.heapIdx = currentIndex
                currentIndex = leftChildIndex
            } else {
                if (rightChildCost >= currentCost) {
                    break
                }
                // 将右子节点上移
                heap[currentIndex] = rightChildNode!!
                rightChildNode.heapIdx = currentIndex
                currentIndex = rightChildIndex
            }
        }
        // 将当前节点放置在正确的位置
        heap[currentIndex] = currentNode
        currentNode.heapIdx = currentIndex
    }

    /**
     * e
     */
    fun isEmpty(): Boolean {
        return size == 0
    }
}