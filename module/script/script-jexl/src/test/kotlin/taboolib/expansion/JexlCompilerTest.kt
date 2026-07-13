package taboolib.expansion

import org.apache.commons.jexl3.JexlException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class JexlCompilerTest {

    @Test
    fun `rebuilds the engine when strict mode changes after first compilation`() {
        val compiler = JexlCompiler()
        val initialEngine = compiler.jexlEngine

        assertNull(compiler.compileToExpression("missing").eval())

        compiler.strict(true)
        val strictEngine = compiler.jexlEngine

        assertNotSame(initialEngine, strictEngine)
        assertSame(strictEngine, compiler.jexlEngine)
        assertThrows(JexlException::class.java) {
            compiler.compileToExpression("missing").eval()
        }
    }

    @Test
    fun `applies namespace changes made after first compilation`() {
        val compiler = JexlCompiler()
        assertEquals(2, compiler.compileToExpression("1 + 1").eval())

        compiler.namespace(mapOf("tools" to Tools(21)))
        assertEquals(21, compiler.compileToExpression("tools:answer()").eval())

        compiler.namespace(mapOf("tools" to Tools(42)))
        assertEquals(42, compiler.compileToExpression("tools:answer()").eval())
    }

    class Tools(private val answer: Int) {

        fun answer(): Int {
            return answer
        }
    }
}
