package taboolib.module.incision.weaver

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.AnalyzerException
import org.objectweb.asm.tree.analysis.BasicValue
import org.objectweb.asm.tree.analysis.BasicVerifier

/**
 * Tree-API 织入后帧一致性预检。
 *
 * 在 [ClassNode.accept] 到 [org.objectweb.asm.ClassWriter] 之前跑一遍：
 * 若任何方法抛 [AnalyzerException]，说明插入的 InsnList 破坏了栈 / 局部变量一致性，
 * 此时应回退原字节码而不是把坏 class 喂给 JVM ——否则得到的是 runtime VerifyError。
 *
 * 使用 [BasicVerifier] 而非简易 [org.objectweb.asm.tree.analysis.BasicInterpreter]：
 * BasicVerifier 额外校验 INVOKE 参数类型、GETFIELD owner 类型等，更贴近 JVM 字节码校验器。
 *
 * 返回的 [Report] 只保留发生问题的方法及其首错（同方法多错不重复收集，降低噪音）。
 */
object FrameVerifier {

    data class Report(
        val ok: Boolean,
        val failures: List<Failure>,
    ) {
        fun summary(): String = if (ok) "ok" else failures.joinToString("; ") { it.shortDesc() }
    }

    data class Failure(
        val method: String,
        val descriptor: String,
        val message: String,
        val cause: Throwable,
    ) {
        fun shortDesc(): String = "$method$descriptor → $message"
    }

    /**
     * 跑 Analyzer<BasicValue> over 所有方法。抽象 / 原生方法跳过（没有方法体）。
     *
     * @param owner 类 internal name（用于 BasicVerifier.newOperation 识别 this 类型）
     */
    fun verify(node: ClassNode): Report {
        val owner = node.name
        val failures = mutableListOf<Failure>()
        for (m in node.methods) {
            if ((m.access and (org.objectweb.asm.Opcodes.ACC_ABSTRACT or org.objectweb.asm.Opcodes.ACC_NATIVE)) != 0) continue
            val f = verifyMethod(owner, m)
            if (f != null) failures += f
        }
        return Report(failures.isEmpty(), failures)
    }

    /**
     * 单方法校验。[BasicVerifier] 需要类层级 Resolver 才能正确判 CHECKCAST / INVOKE
     * owner 继承关系；此处使用默认实现即可——默认假设任何两个引用兼容到 Object，
     * 对"不破坏栈结构"这个目标足够。
     */
    private fun verifyMethod(owner: String, m: MethodNode): Failure? {
        return try {
            val analyzer = Analyzer(BasicVerifier())
            analyzer.analyze(owner, m)
            null
        } catch (e: AnalyzerException) {
            Failure(m.name, m.desc, "${e.javaClass.simpleName}: ${e.message}", e)
        } catch (t: Throwable) {
            Failure(m.name, m.desc, "${t.javaClass.simpleName}: ${t.message}", t)
        }
    }
}
