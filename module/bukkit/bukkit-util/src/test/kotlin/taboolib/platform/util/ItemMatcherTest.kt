package taboolib.platform.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ItemMatcherTest {

    @Test
    fun `insufficient amount produces no removal plan`() {
        val entries = listOf("first" to 2, "second" to 3)

        val plan = planRemoval(6, entries.asSequence()) { it.second }

        assertNull(plan)
        assertEquals(listOf("first" to 2, "second" to 3), entries)
    }

    @Test
    fun `removal plan takes exact amount across stacks`() {
        val entries = sequenceOf("first" to 3, "second" to 5)

        val plan = planRemoval(6, entries) { it.second }

        assertEquals(
            listOf(RemovalPlan("first" to 3, 3), RemovalPlan("second" to 5, 3)),
            plan
        )
    }

    @Test
    fun `non-positive amount produces an empty plan`() {
        assertEquals(emptyList<RemovalPlan<Int>>(), planRemoval(0, sequenceOf(2, 3)) { it })
    }

    @Test
    fun `planning stops after enough items are found`() {
        var evaluations = 0
        val plan = planRemoval(4, sequenceOf(2, 3, 4)) {
            evaluations++
            it
        }

        assertEquals(2, evaluations)
        assertEquals(listOf(2, 2), plan?.map { it.amount })
    }
}
