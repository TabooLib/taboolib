package taboolib.module.incision.weaver

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TryCatchBlockNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode
import taboolib.module.incision.diagnostic.Forensics

/**
 * Side-car bodies 类生成器。
 *
 * 对于目标类 `Foo`，当其方法被 SPLICE 织入（原方法体被 wrapper 替换）后，
 * 本生成器将原方法体的指令流复制到一个同包伴生类 `Foo$$IncisionBodies` 的
 * static 方法中，签名统一为：
 *
 * ```
 * static Object <name>$body(Object self, Object[] args)
 * ```
 *
 * 这样 [taboolib.module.incision.runtime] 的 `proceed()` / `proceedResult()`
 * 可以通过反射调用 bodies 方法，获取原方法逻辑的执行结果。
 *
 * 实现要点：
 * - 接收**织入前**的原始字节码，复制指令流安全无递归
 * - slot 0/1 保留给 self/args 入参；slot 2 保存 CAST 后的 `_self`；
 *   slot 3.. 保存拆箱后的参数；原方法指令中所有 local slot 统一偏移 +2
 * - 返回指令全部替换为装箱 + ARETURN
 * - try-catch 表通过 label map 一并映射
 * - COMPUTE_FRAMES 自动重算栈帧，不处理原 FrameNode
 */
object BodiesClassGenerator {

    const val BODIES_SUFFIX = "\$\$IncisionBodies"
    const val BODY_METHOD_SUFFIX = "\$body"

    /** 统一的 body 方法描述符 */
    const val BODY_DESC = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"

    /**
     * 生成 side-car bodies 类。
     *
     * @param originalBytes 目标类的原始字节码（织入前）
     * @param ownerInternalName 目标类 JVM 内部名，如 "com/example/Foo"
     * @param targetMethods 需要生成 body 的方法集合，元素为 (name, descriptor)
     * @return bodies 类字节码；若无任何方法可生成则返回 null
     */
    fun generate(
        originalBytes: ByteArray,
        ownerInternalName: String,
        targetMethods: Set<Pair<String, String>>,
    ): ByteArray? {
        if (targetMethods.isEmpty()) return null

        // 读入原类
        val reader = ClassReader(originalBytes)
        val originalNode = ClassNode()
        reader.accept(originalNode, ClassReader.EXPAND_FRAMES)

        // 构造 bodies 类
        val bodiesNode = ClassNode()
        bodiesNode.version = originalNode.version
        bodiesNode.access = ACC_PUBLIC or ACC_SYNTHETIC
        bodiesNode.name = ownerInternalName + BODIES_SUFFIX
        bodiesNode.superName = "java/lang/Object"

        // 收集 private 字段 → 用于将 GETFIELD/PUTFIELD 替换为 getter/setter
        val privateFields = originalNode.fields
            ?.filter { it.access and ACC_PRIVATE != 0 }
            ?.associate { it.name to it.desc }
            ?: emptyMap()

        var generated = 0
        for (method in originalNode.methods) {
            val key = method.name to method.desc
            if (key !in targetMethods) continue
            if (method.access and ACC_ABSTRACT != 0) continue
            if (method.access and ACC_NATIVE != 0) continue

            val bodyMethod = generateBodyMethod(method, ownerInternalName, privateFields)
            if (bodyMethod != null) {
                bodiesNode.methods.add(bodyMethod)
                generated++
            }
        }

        if (generated == 0) return null

        val writer = object : ClassWriter(COMPUTE_MAXS or COMPUTE_FRAMES) {
            override fun getCommonSuperClass(type1: String, type2: String): String {
                // 保守但安全：frame 校验对象类型时统一退化到 Object
                return "java/lang/Object"
            }
        }
        bodiesNode.accept(writer)
        return writer.toByteArray()
    }

    // ─────────────────────────────────────────────────────────────────

    private fun generateBodyMethod(original: MethodNode, ownerInternal: String, privateFields: Map<String, String>): MethodNode? {
        if (original.access and ACC_STATIC != 0) return null

        val argTypes = Type.getArgumentTypes(original.desc)
        val returnType = Type.getReturnType(original.desc)

        val body = MethodNode(
            ACC_PUBLIC or ACC_STATIC,
            original.name + BODY_METHOD_SUFFIX,
            BODY_DESC,
            null,
            original.exceptions?.toTypedArray()
        )

        // 预检：不支持 INVOKESPECIAL 调用 owner 自身 private/实例方法（除 <init>）
        if (hasUnsupportedInvokeSpecial(original, ownerInternal)) {
            Forensics.debug(
                "BodiesClassGenerator: 跳过 ${ownerInternal}.${original.name}${original.desc} " +
                    "(包含 INVOKESPECIAL 到自身方法，static 上下文不合法)"
            )
            return null
        }

        val insns = body.instructions

        // Prologue: (Object self, Object[] args) → _self + 拆箱后的参数
        // slot 0 = self, slot 1 = args, slot 2 = _self(owner), slot 3.. = unboxed args
        insns.add(VarInsnNode(ALOAD, 0))
        insns.add(TypeInsnNode(CHECKCAST, ownerInternal))
        insns.add(VarInsnNode(ASTORE, 2))

        var nextSlot = 3
        for ((i, argType) in argTypes.withIndex()) {
            insns.add(VarInsnNode(ALOAD, 1))
            insns.add(pushInt(i))
            insns.add(InsnNode(AALOAD))
            emitUnbox(insns, argType)
            insns.add(VarInsnNode(argType.getOpcode(ISTORE), nextSlot))
            nextSlot += argType.size
        }

        // 复制原方法指令流（slot +2、return 装箱替换、label 映射）
        val labelMap = HashMap<LabelNode, LabelNode>()
        for (insn in original.instructions) {
            if (insn is LabelNode) labelMap[insn] = LabelNode()
        }

        cloneInstructionsInto(insns, original, returnType, labelMap, ownerInternal, privateFields)

        // try-catch 表映射
        for (tcb in original.tryCatchBlocks) {
            val start = labelMap[tcb.start] ?: continue
            val end = labelMap[tcb.end] ?: continue
            val handler = labelMap[tcb.handler] ?: continue
            body.tryCatchBlocks.add(TryCatchBlockNode(start, end, handler, tcb.type))
        }

        // COMPUTE_MAXS / COMPUTE_FRAMES 会重算
        body.maxStack = 0
        body.maxLocals = 0

        return body
    }

    private fun hasUnsupportedInvokeSpecial(original: MethodNode, ownerInternal: String): Boolean {
        for (insn in original.instructions) {
            if (insn is MethodInsnNode && insn.opcode == INVOKESPECIAL) {
                if (insn.owner == ownerInternal && insn.name != "<init>") {
                    return true
                }
            }
        }
        return false
    }

    /** JvmtiBackend 在 JNI 注册后的内部类名（可能被 Shadow 重定位） */
    private const val JVMTI_BACKEND = "taboolib/module/incision/loader/JvmtiBackend"

    /**
     * 克隆原方法指令流到 [out]，做 slot 偏移、return 替换、private 字段访问替换。
     *
     * 规则：
     * - [VarInsnNode].var 与 [IincInsnNode].var：+2
     * - xRETURN（基本类型）：装箱 + ARETURN
     * - RETURN（void）：ACONST_NULL + ARETURN
     * - ARETURN：保持
     * - GETFIELD/PUTFIELD 访问 private 字段：替换为 JNI 层 nFieldGet/nFieldSet
     */
    private fun cloneInstructionsInto(
        out: InsnList,
        original: MethodNode,
        returnType: Type,
        labelMap: HashMap<LabelNode, LabelNode>,
        ownerInternal: String,
        privateFields: Map<String, String>,
    ) {
        for (insn in original.instructions) {
            val cloned: AbstractInsnNode = insn.clone(labelMap)

            when (cloned) {
                is VarInsnNode -> cloned.`var` += 2
                is IincInsnNode -> cloned.`var` += 2
            }

            // private 字段访问 → 通过 C 层 JNI 绕过访问控制
            if (cloned is FieldInsnNode && cloned.owner == ownerInternal && cloned.name in privateFields) {
                when (cloned.opcode) {
                    GETFIELD -> {
                        // 栈顶: objectref → 需要: objectref, ownerClass, fieldName, fieldDesc → Object
                        // 获取 owner 的 Class 对象
                        out.add(LdcInsnNode(Type.getObjectType(ownerInternal)))
                        out.add(LdcInsnNode(cloned.name))
                        out.add(LdcInsnNode(cloned.desc))
                        out.add(MethodInsnNode(
                            INVOKESTATIC, JVMTI_BACKEND, "nFieldGet",
                            "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",
                            false
                        ))
                        // 返回的是 Object（已装箱），需要拆箱回原始类型
                        val fieldType = Type.getType(cloned.desc)
                        emitUnbox(out, fieldType)
                        continue
                    }
                    PUTFIELD -> {
                        // 栈顶: objectref, value → 需要: objectref, ownerClass, fieldName, fieldDesc, value(boxed)
                        // 先装箱 value
                        val fieldType = Type.getType(cloned.desc)
                        emitBox(out, fieldType)
                        // 栈: objectref, boxedValue
                        // 需要重新排列为: objectref, ownerClass, fieldName, fieldDesc, boxedValue
                        // 用临时变量暂存 boxedValue
                        out.add(VarInsnNode(ASTORE, 0)) // 暂存到 slot 0（self 参数，此时已不需要）
                        // 栈: objectref
                        out.add(LdcInsnNode(Type.getObjectType(ownerInternal)))
                        out.add(LdcInsnNode(cloned.name))
                        out.add(LdcInsnNode(cloned.desc))
                        out.add(VarInsnNode(ALOAD, 0)) // 取回 boxedValue
                        out.add(MethodInsnNode(
                            INVOKESTATIC, JVMTI_BACKEND, "nFieldSet",
                            "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V",
                            false
                        ))
                        continue
                    }
                    GETSTATIC -> {
                        // 无栈顶对象，直接调用 nStaticFieldGet
                        out.add(LdcInsnNode(Type.getObjectType(ownerInternal)))
                        out.add(LdcInsnNode(cloned.name))
                        out.add(LdcInsnNode(cloned.desc))
                        out.add(MethodInsnNode(
                            INVOKESTATIC, JVMTI_BACKEND, "nStaticFieldGet",
                            "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",
                            false
                        ))
                        val fieldType = Type.getType(cloned.desc)
                        emitUnbox(out, fieldType)
                        continue
                    }
                    PUTSTATIC -> {
                        // 栈顶: value → 需要: ownerClass, fieldName, fieldDesc, value(boxed)
                        val fieldType = Type.getType(cloned.desc)
                        emitBox(out, fieldType)
                        out.add(VarInsnNode(ASTORE, 0))
                        out.add(LdcInsnNode(Type.getObjectType(ownerInternal)))
                        out.add(LdcInsnNode(cloned.name))
                        out.add(LdcInsnNode(cloned.desc))
                        out.add(VarInsnNode(ALOAD, 0))
                        out.add(MethodInsnNode(
                            INVOKESTATIC, JVMTI_BACKEND, "nStaticFieldSet",
                            "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V",
                            false
                        ))
                        continue
                    }
                }
            }

            if (cloned is InsnNode) {
                when (cloned.opcode) {
                    IRETURN, LRETURN, FRETURN, DRETURN -> {
                        emitBox(out, returnType)
                        out.add(InsnNode(ARETURN))
                        continue
                    }
                    RETURN -> {
                        out.add(InsnNode(ACONST_NULL))
                        out.add(InsnNode(ARETURN))
                        continue
                    }
                    ARETURN -> {
                        out.add(cloned)
                        continue
                    }
                }
            }

            out.add(cloned)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 装箱 / 拆箱 / int 常量

    /**
     * 栈顶是 Object（args[i]），按 [target] 类型拆箱为对应原始值或引用。
     */
    private fun emitUnbox(out: InsnList, target: Type) {
        when (target.sort) {
            Type.BOOLEAN -> {
                out.add(TypeInsnNode(CHECKCAST, "java/lang/Boolean"))
                out.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false))
            }
            Type.BYTE -> {
                out.add(TypeInsnNode(CHECKCAST, "java/lang/Byte"))
                out.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false))
            }
            Type.CHAR -> {
                out.add(TypeInsnNode(CHECKCAST, "java/lang/Character"))
                out.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false))
            }
            Type.SHORT -> {
                out.add(TypeInsnNode(CHECKCAST, "java/lang/Short"))
                out.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false))
            }
            Type.INT -> {
                out.add(TypeInsnNode(CHECKCAST, "java/lang/Integer"))
                out.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false))
            }
            Type.LONG -> {
                out.add(TypeInsnNode(CHECKCAST, "java/lang/Long"))
                out.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false))
            }
            Type.FLOAT -> {
                out.add(TypeInsnNode(CHECKCAST, "java/lang/Float"))
                out.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false))
            }
            Type.DOUBLE -> {
                out.add(TypeInsnNode(CHECKCAST, "java/lang/Double"))
                out.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false))
            }
            Type.OBJECT, Type.ARRAY -> {
                out.add(TypeInsnNode(CHECKCAST, target.internalName))
            }
            else -> error("unsupported type sort: ${target.sort}")
        }
    }

    /**
     * 栈顶是原始类型/引用值，装箱为 Object。
     */
    private fun emitBox(out: InsnList, source: Type) {
        when (source.sort) {
            Type.BOOLEAN -> out.add(MethodInsnNode(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false))
            Type.BYTE -> out.add(MethodInsnNode(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false))
            Type.CHAR -> out.add(MethodInsnNode(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false))
            Type.SHORT -> out.add(MethodInsnNode(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false))
            Type.INT -> out.add(MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false))
            Type.LONG -> out.add(MethodInsnNode(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false))
            Type.FLOAT -> out.add(MethodInsnNode(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false))
            Type.DOUBLE -> out.add(MethodInsnNode(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false))
            Type.OBJECT, Type.ARRAY -> { /* 已是引用，无需装箱 */ }
            Type.VOID -> error("emitBox called with VOID")
            else -> error("unsupported type sort: ${source.sort}")
        }
    }

    /**
     * 产出把 int 常量压栈的最短指令。
     */
    private fun pushInt(value: Int): AbstractInsnNode {
        return when {
            value == -1 -> InsnNode(ICONST_M1)
            value == 0 -> InsnNode(ICONST_0)
            value == 1 -> InsnNode(ICONST_1)
            value == 2 -> InsnNode(ICONST_2)
            value == 3 -> InsnNode(ICONST_3)
            value == 4 -> InsnNode(ICONST_4)
            value == 5 -> InsnNode(ICONST_5)
            value in Byte.MIN_VALUE..Byte.MAX_VALUE -> IntInsnNode(BIPUSH, value)
            value in Short.MIN_VALUE..Short.MAX_VALUE -> IntInsnNode(SIPUSH, value)
            else -> LdcInsnNode(value)
        }
    }
}
