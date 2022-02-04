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
        var heapIdx = value
        val node = heap[heapIdx]
        var var3: Int
        val var2 = node!!.g
        while (heapIdx > 0) {
            var3 = heapIdx - 1 shr 1
            val var4 = heap[var3]
            if (var2 >= var4!!.g) {
                break
            }
            heap[heapIdx] = var4
            var4.heapIdx = heapIdx
            heapIdx = var3
        }
        heap[heapIdx] = node
        node.heapIdx = heapIdx
    }

    /**
     * b
     */
    fun downHeap(value: Int) {
        var var0 = value
        val node = heap[var0]!!
        val cost = node.cost
        while (true) {
            val var3: Int = 1 + (var0 shl 1)
            val var4 = var3 + 1
            if (var3 >= size) {
                break
            }
            val var5 = heap[var3]!!
            val var6 = var5.cost
            var var7: Node?
            var var8: Float
            if (var4 >= size) {
                var7 = null
                var8 = Float.POSITIVE_INFINITY
            } else {
                var7 = heap[var4]!!
                var8 = var7.cost
            }
            if (var6 < var8) {
                if (var6 >= cost) {
                    break
                }
                heap[var0] = var5
                var5.heapIdx = var0
                var0 = var3
            } else {
                if (var8 >= cost) {
                    break
                }
                heap[var0] = var7!!
                var7.heapIdx = var0
                var0 = var4
            }
        }
        heap[var0] = node
        node.heapIdx = var0
    }

    /**
     * e
     */
    fun isEmpty(): Boolean {
        return size == 0
    }
}