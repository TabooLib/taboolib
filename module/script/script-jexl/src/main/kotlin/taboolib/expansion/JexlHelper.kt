@file:Inject
@file:RuntimeDependencies(
    RuntimeDependency(
        "!org.apache.commons:commons-jexl3:3.2.1",
        test = "!org.apache.commons.jexl3_3_2_1.JexlEngine",
        relocate = [
            "!org.apache.commons.jexl3", "!org.apache.commons.jexl3_3_2_1",
            "!org.apache.commons.logging", "!org.apache.commons.logging_1_2"
        ],
        transitive = false
    ),
    RuntimeDependency(
        "!commons-logging:commons-logging:1.2",
        test = "!org.apache.commons.logging_1_2.Log",
        relocate = ["!org.apache.commons.logging", "!org.apache.commons.logging_1_2"],
        transitive = false
    )
)

package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.env.RuntimeDependencies
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