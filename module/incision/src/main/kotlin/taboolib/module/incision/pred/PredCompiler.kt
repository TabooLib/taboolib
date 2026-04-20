package taboolib.module.incision.pred

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import taboolib.module.incision.diagnostic.Trauma
import taboolib.module.incision.loader.JvmtiBackend
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicInteger

/**
 * 谓词字节码编译器：把 [PredAst] 编译成实现 [Predicate] 接口的匿名类，并装载到目标 ClassLoader。
 *
 * 生成策略：
 * - 一个方法 `test(Lcom/.../EvalContext;)Z`，方法体里所有中间表达式以 `Object` 形式驻留栈顶；
 *   最终 `INVOKESTATIC PredOps.truthy(Object)Z` 转为 boolean 返回。
 * - 类型算子、比较、成员访问、下标全部下发到 [PredOps]，保持字节码体积小、栈类型简单。
 * - 类型字面量（`is/ic/ip/it/as` 后的 `T`）走 `PredOps.resolveType(name, cl)`，cl 取自当前线程 contextClassLoader；
 *   若无法解析视为永假分支（asCast 返回 null，类型检查返回 false）。
 *
 * 上下文（[AdviceCtx]）来自调用方，用于：
 * - 选择装载目标 ClassLoader（写到 advice 注册方的插件 CL，后续 script 场景可指向沙箱 CL）
 * - 提供 advice id 用于错误诊断
 * - 通过 [AdviceCtx.extraVars] 扩展顶层变量白名单（默认 `args/this/result/env/site/caller`）
 *
 * 调用方式：
 * ```kotlin
 * val pred = PredCompiler.compile("args[0] is java.lang.String && result != null",
 *                                  AdviceCtx("my.advice.id", pluginClassLoader))
 * // 注册期完成；运行期 dispatcher 直接 pred.test(ctx)
 * ```
 */
object PredCompiler {

    private val seq = AtomicInteger(0)

    private val DEFAULT_VARS = setOf("args", "this", "result", "env", "site", "caller")

    private val OPS_INTERNAL = PredOps::class.java.name.replace('.', '/')
    private val PRED_INTERNAL = Predicate::class.java.name.replace('.', '/')
    private val EVAL_INTERNAL = EvalContext::class.java.name.replace('.', '/')
    private val GEN_PACKAGE = PRED_INTERNAL.substringBeforeLast('/') + "/gen"
    private const val OBJ = "Ljava/lang/Object;"

    /**
     * 入口：源码 → AST → 字节码 → 装载 → 实例。注册期一次性调用，运行期复用返回的 [Predicate]。
     *
     * @param source 谓词源码（DSL 字符串）。
     * @param ctx    [AdviceCtx]，提供装载 ClassLoader、advice id 与变量白名单。
     * @return 已装载并实例化的 [Predicate]，永不返回 null（失败一律抛异常）。
     *
     * @throws taboolib.module.incision.diagnostic.Trauma.Predicate.SyntaxError
     *         词法 / 语法错误，例如未闭合的字符串、意外 token、`expr` 末尾多余内容、`!as` 等不合法组合。
     * @throws taboolib.module.incision.diagnostic.Trauma.Predicate.MethodIndexed
     *         在方法调用结果上做下标访问（如 `args.size[0]`）。
     * @throws taboolib.module.incision.diagnostic.Trauma.Predicate.UndefinedVariable
     *         引用了不在默认 6 项 + [AdviceCtx.extraVars] 之内的顶层变量。
     * @throws taboolib.module.incision.diagnostic.Trauma.Predicate.RuntimeFailure
     *         字节码生成成功但 ClassLoader.defineClass 失败（被包装抛出）。
     *
     * 注：[Trauma.Predicate.UnknownMember] / [Trauma.Predicate.TypeMismatch] 不在编译期抛出，
     * 它们由运行期 [PredOps] 在反射兜底失败时由上层 dispatcher 包装。
     */
    fun compile(source: String, ctx: AdviceCtx): Predicate {
        val ast = PredParser(source).parse()
        validate(source, ast, ctx)
        val (className, bytes) = generate(ast, ctx, source)
        val cls = LoaderHelper.define(ctx.classLoader, className, bytes)
        return cls.getDeclaredConstructor().newInstance() as Predicate
    }

    // ---------- 校验：未定义变量、方法结果做下标已在 Parser 拦截 ----------

    private fun validate(source: String, ast: PredAst, ctx: AdviceCtx) {
        val allowed = DEFAULT_VARS + ctx.extraVars
        walk(ast) { node ->
            if (node is PredAst.Var && node.name !in allowed) {
                throw Trauma.Predicate.UndefinedVariable(source, node.name)
            }
        }
    }

    private fun walk(ast: PredAst, visit: (PredAst) -> Unit) {
        visit(ast)
        when (ast) {
            is PredAst.Or -> ast.items.forEach { walk(it, visit) }
            is PredAst.And -> ast.items.forEach { walk(it, visit) }
            is PredAst.Not -> walk(ast.target, visit)
            is PredAst.Cmp -> {
                walk(ast.left, visit); walk(ast.right, visit)
            }

            is PredAst.TypeCheck -> walk(ast.target, visit)
            is PredAst.As -> walk(ast.target, visit)
            is PredAst.PropertyAccess -> walk(ast.receiver, visit)
            is PredAst.MethodCall -> {
                walk(ast.receiver, visit); ast.args.forEach { walk(it, visit) }
            }

            is PredAst.Index -> {
                walk(ast.receiver, visit); walk(ast.index, visit)
            }

            is PredAst.SafeAccess -> walk(ast.target, visit)
            is PredAst.Paren -> walk(ast.inner, visit)
            is PredAst.Var, is PredAst.Literal -> Unit
        }
    }

    // ---------- 生成 ----------

    private fun generate(ast: PredAst, ctx: AdviceCtx, source: String): Pair<String, ByteArray> {
        val id = seq.incrementAndGet()
        val internal = "$GEN_PACKAGE/Pred\$$id"
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL or Opcodes.ACC_SYNTHETIC,
            internal, null, "java/lang/Object",
            arrayOf(PRED_INTERNAL),
        )
        // <init>
        val init = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
        init.visitCode()
        init.visitVarInsn(Opcodes.ALOAD, 0)
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        init.visitInsn(Opcodes.RETURN)
        init.visitMaxs(0, 0)
        init.visitEnd()
        // test(Lcom/.../EvalContext;)Z
        val mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC,
            "test",
            "(L$EVAL_INTERNAL;)Z",
            null, null,
        )
        mv.visitCode()
        Emitter(mv, ctx).emit(ast)
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, OPS_INTERNAL, "truthy", "(Ljava/lang/Object;)Z", false)
        mv.visitInsn(Opcodes.IRETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        cw.visitEnd()
        return internal.replace('/', '.') to cw.toByteArray()
    }

    // ---------- 表达式发射器 ----------

    private class Emitter(val mv: MethodVisitor, val ctx: AdviceCtx) {

        /** 任一 emit 调用结束后，栈顶为一个 Object（含 Boolean 装箱）。 */
        fun emit(ast: PredAst) {
            when (ast) {
                is PredAst.Or -> emitOr(ast)
                is PredAst.And -> emitAnd(ast)
                is PredAst.Not -> emitNot(ast)
                is PredAst.Cmp -> emitCmp(ast)
                is PredAst.TypeCheck -> emitTypeCheck(ast)
                is PredAst.As -> emitAs(ast)
                is PredAst.PropertyAccess -> emitProperty(ast.receiver, ast.name, safe = false)
                is PredAst.MethodCall -> emitMethodCall(ast.receiver, ast.name, ast.args, safe = false)
                is PredAst.Index -> {
                    emit(ast.receiver); emit(ast.index); invokeOps("index", "(${OBJ}${OBJ})${OBJ}")
                }

                is PredAst.SafeAccess -> emitSafe(ast.target)
                is PredAst.Paren -> emit(ast.inner)
                is PredAst.Var -> emitVar(ast.name)
                is PredAst.Literal -> emitLiteral(ast.value)
            }
        }

        private fun emitOr(node: PredAst.Or) {
            // 短路：第一个为 true 即 Boolean.TRUE
            val end = Label()
            val pushTrue = Label()
            for (i in node.items.indices) {
                emit(node.items[i])
                if (i == node.items.size - 1) break
                // dup; truthy?  if truthy → goto pushTrue（保留栈顶值再跳）
                mv.visitInsn(Opcodes.DUP)
                invokeOps("truthy", "(${OBJ})Z")
                mv.visitJumpInsn(Opcodes.IFNE, pushTrue)
                mv.visitInsn(Opcodes.POP) // 丢弃假值，继续下一项
            }
            mv.visitJumpInsn(Opcodes.GOTO, end)
            mv.visitLabel(pushTrue)
            // 栈顶已是上一项的（真）值，原样保留即可
            mv.visitLabel(end)
        }

        private fun emitAnd(node: PredAst.And) {
            val end = Label()
            val pushFalse = Label()
            for (i in node.items.indices) {
                emit(node.items[i])
                if (i == node.items.size - 1) break
                mv.visitInsn(Opcodes.DUP)
                invokeOps("truthy", "(${OBJ})Z")
                mv.visitJumpInsn(Opcodes.IFEQ, pushFalse)
                mv.visitInsn(Opcodes.POP)
            }
            mv.visitJumpInsn(Opcodes.GOTO, end)
            mv.visitLabel(pushFalse)
            // 栈顶是假值，保留
            mv.visitLabel(end)
        }

        private fun emitNot(node: PredAst.Not) {
            emit(node.target)
            invokeOps("truthy", "(${OBJ})Z")
            // boolean → !boolean → Boolean
            val falseL = Label();
            val end = Label()
            mv.visitJumpInsn(Opcodes.IFNE, falseL)
            getStaticBoolean(true)
            mv.visitJumpInsn(Opcodes.GOTO, end)
            mv.visitLabel(falseL)
            getStaticBoolean(false)
            mv.visitLabel(end)
        }

        private fun emitCmp(node: PredAst.Cmp) {
            emit(node.left)
            emit(node.right)
            val (name, desc) = when (node.op) {
                "==" -> "eq" to "(${OBJ}${OBJ})Z"
                "!=" -> "neq" to "(${OBJ}${OBJ})Z"
                "<" -> "lt" to "(${OBJ}${OBJ})Z"
                ">" -> "gt" to "(${OBJ}${OBJ})Z"
                "<=" -> "le" to "(${OBJ}${OBJ})Z"
                ">=" -> "ge" to "(${OBJ}${OBJ})Z"
                "matches" -> "matches" to "(${OBJ}${OBJ})Z"
                "in" -> "contains" to "(${OBJ}${OBJ})Z"
                else -> error("unknown cmp op ${node.op}")
            }
            invokeOps(name, desc)
            boxBoolean()
        }

        private fun emitTypeCheck(node: PredAst.TypeCheck) {
            emit(node.target)
            emitResolveType(node.typeName)
            val name = when (node.kind) {
                PredAst.TypeCheck.Kind.IS -> "isInstanceOf"
                PredAst.TypeCheck.Kind.IC -> "isInstanceChild"
                PredAst.TypeCheck.Kind.IP -> "isAssignable"
                PredAst.TypeCheck.Kind.IT -> "isExactType"
            }
            invokeOps(name, "(${OBJ}Ljava/lang/Class;)Z")
            if (node.negate) {
                val falseL = Label();
                val end = Label()
                mv.visitJumpInsn(Opcodes.IFNE, falseL)
                getStaticBoolean(true); mv.visitJumpInsn(Opcodes.GOTO, end)
                mv.visitLabel(falseL); getStaticBoolean(false)
                mv.visitLabel(end)
            } else {
                boxBoolean()
            }
        }

        private fun emitAs(node: PredAst.As) {
            emit(node.target)
            emitResolveType(node.typeName)
            invokeOps("asCast", "(${OBJ}Ljava/lang/Class;)${OBJ}")
        }

        private fun emitProperty(receiver: PredAst, name: String, safe: Boolean) {
            emit(receiver)
            if (safe) emitNullShortCircuit { /* falls through with object */ }
            mv.visitLdcInsn(name)
            invokeOps("getProperty", "(${OBJ}Ljava/lang/String;)${OBJ}")
        }

        private fun emitMethodCall(receiver: PredAst, name: String, args: List<PredAst>, safe: Boolean) {
            emit(receiver)
            if (safe) emitNullShortCircuit { /* */ }
            mv.visitLdcInsn(name)
            // new Object[args.size]
            pushInt(args.size)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
            for ((i, a) in args.withIndex()) {
                mv.visitInsn(Opcodes.DUP)
                pushInt(i)
                emit(a)
                mv.visitInsn(Opcodes.AASTORE)
            }
            invokeOps("callMethod", "(${OBJ}Ljava/lang/String;[Ljava/lang/Object;)${OBJ}")
        }

        /**
         * `?.`：把内部 PropertyAccess/MethodCall 在 receiver=null 时短路为 null。
         * 实现：包装一层判空 —— 如果 receiver 就是 null 直接走 null 分支。
         */
        private fun emitSafe(inner: PredAst) {
            when (inner) {
                is PredAst.PropertyAccess -> {
                    emit(inner.receiver)
                    val nullL = Label();
                    val end = Label()
                    mv.visitInsn(Opcodes.DUP)
                    mv.visitJumpInsn(Opcodes.IFNULL, nullL)
                    mv.visitLdcInsn(inner.name)
                    invokeOps("getProperty", "(${OBJ}Ljava/lang/String;)${OBJ}")
                    mv.visitJumpInsn(Opcodes.GOTO, end)
                    mv.visitLabel(nullL)
                    // 栈顶是 null，保留
                    mv.visitLabel(end)
                }

                is PredAst.MethodCall -> {
                    emit(inner.receiver)
                    val nullL = Label();
                    val end = Label()
                    mv.visitInsn(Opcodes.DUP)
                    mv.visitJumpInsn(Opcodes.IFNULL, nullL)
                    mv.visitLdcInsn(inner.name)
                    pushInt(inner.args.size)
                    mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
                    for ((i, a) in inner.args.withIndex()) {
                        mv.visitInsn(Opcodes.DUP)
                        pushInt(i)
                        emit(a)
                        mv.visitInsn(Opcodes.AASTORE)
                    }
                    invokeOps("callMethod", "(${OBJ}Ljava/lang/String;[Ljava/lang/Object;)${OBJ}")
                    mv.visitJumpInsn(Opcodes.GOTO, end)
                    mv.visitLabel(nullL)
                    mv.visitLabel(end)
                }

                else -> emit(inner) // 非访问节点的安全前缀语义等价于裸求值
            }
        }

        /** 占位：保留以便未来补充更复杂的短路路径（当前 emitSafe 中已用内联实现）。 */
        private inline fun emitNullShortCircuit(crossinline body: () -> Unit) {
            body()
        }

        private fun emitVar(name: String) {
            // ALOAD 1 = EvalContext
            mv.visitVarInsn(Opcodes.ALOAD, 1)
            when (name) {
                "args" -> {
                    // 顶层 args 暴露为 Object[]（通过 EvalContext.argCount + argAt 循环填充）
                    emitCollectArgs()
                }

                "this" -> mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EVAL_INTERNAL, "thisRef", "()${OBJ}", true)
                "result" -> mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EVAL_INTERNAL, "result", "()${OBJ}", true)
                "env" -> mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EVAL_INTERNAL, "env", "()Ljava/util/Map;", true)
                "site" -> mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EVAL_INTERNAL, "site", "()${OBJ}", true)
                "caller" -> mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EVAL_INTERNAL, "caller", "()${OBJ}", true)
                else -> {
                    // 自定义变量：尝试 env().get(name)
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EVAL_INTERNAL, "env", "()Ljava/util/Map;", true)
                    mv.visitLdcInsn(name)
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(${OBJ})${OBJ}", true)
                }
            }
        }

        private fun emitCollectArgs() {
            // 入参：栈顶为 EvalContext 副本（emitVar 已 ALOAD 1 一次）。
            // 直接丢弃，改用局部变量法构造 Object[]。slots: 2=n, 3=arr, 4=i
            mv.visitInsn(Opcodes.POP)
            mv.visitVarInsn(Opcodes.ALOAD, 1)
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EVAL_INTERNAL, "argCount", "()I", true)
            mv.visitVarInsn(Opcodes.ISTORE, 2)
            mv.visitVarInsn(Opcodes.ILOAD, 2)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
            mv.visitVarInsn(Opcodes.ASTORE, 3)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitVarInsn(Opcodes.ISTORE, 4)
            val loop = Label();
            val end = Label()
            mv.visitLabel(loop)
            mv.visitVarInsn(Opcodes.ILOAD, 4)
            mv.visitVarInsn(Opcodes.ILOAD, 2)
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, end)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitVarInsn(Opcodes.ILOAD, 4)
            mv.visitVarInsn(Opcodes.ALOAD, 1)
            mv.visitVarInsn(Opcodes.ILOAD, 4)
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EVAL_INTERNAL, "argAt", "(I)${OBJ}", true)
            mv.visitInsn(Opcodes.AASTORE)
            mv.visitIincInsn(4, 1)
            mv.visitJumpInsn(Opcodes.GOTO, loop)
            mv.visitLabel(end)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
        }

        private fun emitLiteral(v: Any?) {
            when (v) {
                null -> mv.visitInsn(Opcodes.ACONST_NULL)
                is Boolean -> getStaticBoolean(v)
                is Int -> {
                    pushInt(v); mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
                }

                is Long -> {
                    mv.visitLdcInsn(v); mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false)
                }

                is Double -> {
                    mv.visitLdcInsn(v); mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false)
                }

                is String -> mv.visitLdcInsn(v)
                else -> mv.visitLdcInsn(v.toString())
            }
        }

        private fun emitResolveType(typeName: String) {
            mv.visitLdcInsn(typeName)
            // 把当前线程 contextClassLoader 传入
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "getContextClassLoader", "()Ljava/lang/ClassLoader;", false)
            invokeOps("resolveType", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/Class;")
        }

        // helpers
        private fun invokeOps(name: String, desc: String) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, OPS_INTERNAL, name, desc, false)
        }

        private fun boxBoolean() {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false)
        }

        private fun getStaticBoolean(b: Boolean) {
            mv.visitFieldInsn(
                Opcodes.GETSTATIC, "java/lang/Boolean",
                if (b) "TRUE" else "FALSE", "Ljava/lang/Boolean;",
            )
        }

        private fun pushInt(i: Int) {
            when {
                i in -1..5 -> mv.visitInsn(Opcodes.ICONST_0 + i)
                i in Byte.MIN_VALUE..Byte.MAX_VALUE -> mv.visitIntInsn(Opcodes.BIPUSH, i)
                i in Short.MIN_VALUE..Short.MAX_VALUE -> mv.visitIntInsn(Opcodes.SIPUSH, i)
                else -> mv.visitLdcInsn(i)
            }
        }
    }

    // ---------- 类装载器：反射优先；JDK 9+ 模块边界失败则退到 JNI DefineClass ----------

    private object LoaderHelper {
        private val defineMethod: Method? by lazy {
            try {
                val m = ClassLoader::class.java.getDeclaredMethod(
                    "defineClass",
                    String::class.java,
                    ByteArray::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                )
                m.isAccessible = true
                m
            } catch (_: Throwable) {
                null
            }
        }

        fun define(cl: ClassLoader, name: String, bytes: ByteArray): Class<*> {
            val binaryName = name.replace('/', '.')
            try {
                return Class.forName(binaryName, false, cl)
            } catch (_: Throwable) {
            }
            val reflectError: Throwable? = defineMethod?.let { m ->
                try {
                    return m.invoke(cl, binaryName, bytes, 0, bytes.size) as Class<*>
                } catch (t: Throwable) {
                    t
                }
            }
            // JDK 9+ 或反射被禁：退到 native JNI DefineClass（不受模块系统限制）
            try {
                val cls = JvmtiBackend.defineClassInClassLoader(cl, name, bytes)
                if (cls != null) return cls
            } catch (_: Throwable) {
                // native 不可用时继续抛原反射异常
            }
            throw Trauma.Predicate.RuntimeFailure(
                "<gen $name>", null,
                reflectError ?: IllegalStateException("defineClass unavailable (reflection + JVMTI both failed)")
            )
        }
    }
}
