@file:RuntimeDependency(
    "!org.apache.commons:commons-jexl3:3.2.1",
    test = "!org.apache.commons.jexl3_3_2_1.JexlEngine",
    relocate = ["!org.apache.commons.jexl3", "!org.apache.commons.jexl3_3_2_1"],
    transitive = false
)

package taboolib.expansion

import taboolib.common.env.RuntimeDependency
import taboolib.common.util.unsafeLazy

val defaultJexlCompiler by unsafeLazy { JexlCompiler.new() }

/**
 * 将字符串编译为 JexlScript
 */
fun String.compileToScript(compiler: JexlCompiler = defaultJexlCompiler) = compiler.compileToScript(this)

/**
 * 将字符串编译为 JexlScript
 */
fun String.compileToExpression(compiler: JexlCompiler = defaultJexlCompiler) = compiler.compileToExpression(this)