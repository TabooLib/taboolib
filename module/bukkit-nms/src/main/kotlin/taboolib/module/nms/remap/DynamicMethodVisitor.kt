package taboolib.module.nms.remap

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import taboolib.common.reflect.ClassHelper
import taboolib.module.nms.MinecraftVersion

/**
 * 检测 dynamic() 调用模式并替换为直接 JVM 指令的方法访问器
 *
 * 状态机：
 * IDLE → 看到 GETSTATIC DynamicOpcode.* → SEEN_OPCODE
 * SEEN_OPCODE → 看到 LDC String → SEEN_DESCRIPTOR
 * SEEN_DESCRIPTOR → 看到 INVOKESTATIC RemapDynamicKt.dynamic → 触发转换
 *
 * 所有中间指令（vararg 数组构造）正常 emit，仅在最终 INVOKESTATIC 处生成替换字节码。
 *
 * @author sky
 */
class DynamicMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor,
    private val remapTranslation: RemapTranslation,
    private val nextLocalVar: Int
) : MethodVisitor(api, methodVisitor) {

    private enum class State { IDLE, SEEN_OPCODE, SEEN_DESCRIPTOR }

    private var state = State.IDLE
    private var capturedOpcode: DynamicOpcode? = null
    private var capturedDescriptor: String? = null

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        if (opcode == Opcodes.GETSTATIC && owner == DYNAMIC_OPCODE_OWNER && descriptor == dynamicOpcodeDesc) {
            state = State.SEEN_OPCODE
            capturedOpcode = DynamicOpcode.valueOf(name!!)
            super.visitFieldInsn(opcode, owner, name, descriptor)
            return
        }
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitLdcInsn(value: Any?) {
        if (state == State.SEEN_OPCODE && value is String) {
            state = State.SEEN_DESCRIPTOR
            capturedDescriptor = value
            super.visitLdcInsn(value)
            return
        }
        if (state == State.SEEN_OPCODE) {
            resetState()
        }
        super.visitLdcInsn(value)
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
        if (state == State.SEEN_DESCRIPTOR
            && opcode == Opcodes.INVOKESTATIC
            && owner == DYNAMIC_KT_OWNER
            && name == "dynamic"
            && descriptor == dynamicMethodDesc
        ) {
            try {
                emitDynamicTransform()
            } catch (e: Throwable) {
                // 单个 dynamic() 转换失败，保留原始调用，运行时会抛出明确错误
                System.err.println("[DynamicMethodVisitor] dynamic() 转换失败: ${capturedDescriptor}")
                e.printStackTrace()
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
            resetState()
            return
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    private fun resetState() {
        state = State.IDLE
        capturedOpcode = null
        capturedDescriptor = null
    }

    /**
     * 生成替换字节码
     *
     * 栈顶此时为：... DynamicOpcode, String, Object[]
     * 1. 将 Object[] 存入临时局部变量
     * 2. POP String
     * 3. POP DynamicOpcode
     * 4. 根据 opcode 类型生成对应的直接 JVM 指令
     */
    private fun emitDynamicTransform() {
        val dynOpcode = capturedOpcode!!
        val rawDescriptor = capturedDescriptor!!.replace("{version}", MinecraftVersion.minecraftVersion)
        val parsed = parseDescriptor(dynOpcode, rawDescriptor)
        val normalizedDesc = normalizeDescriptor(parsed.memberDescriptor, parsed.isField)
        val originalOwner = parsed.className.replace('.', '/')
        val translatedOwner = remapTranslation.translate(originalOwner)
        val translatedDesc = translateDescriptor(normalizedDesc)
        val translatedMember = when {
            parsed.isConstructor -> "<init>"
            parsed.isField -> tryMapName { remapTranslation.mapFieldName(originalOwner, parsed.memberName!!, normalizedDesc) } ?: parsed.memberName!!
            else -> tryMapName { remapTranslation.mapMethodName(originalOwner, parsed.memberName!!, normalizedDesc) } ?: parsed.memberName!!
        }
        val tempVar = nextLocalVar
        mv.visitVarInsn(Opcodes.ASTORE, tempVar)
        mv.visitInsn(Opcodes.POP)
        mv.visitInsn(Opcodes.POP)
        when (dynOpcode) {
            DynamicOpcode.INVOKEVIRTUAL -> emitInvoke(Opcodes.INVOKEVIRTUAL, translatedOwner, translatedMember, translatedDesc, tempVar, hasInstance = true)
            DynamicOpcode.INVOKESTATIC -> emitInvoke(Opcodes.INVOKESTATIC, translatedOwner, translatedMember, translatedDesc, tempVar, hasInstance = false)
            DynamicOpcode.INVOKESPECIAL -> emitInvokeSpecial(translatedOwner, translatedDesc, tempVar)
            DynamicOpcode.GETFIELD -> emitGetField(translatedOwner, translatedMember, translatedDesc, tempVar)
            DynamicOpcode.PUTFIELD -> emitPutField(translatedOwner, translatedMember, translatedDesc, tempVar)
            DynamicOpcode.GETSTATIC -> emitGetStatic(translatedOwner, translatedMember, translatedDesc, tempVar)
            DynamicOpcode.PUTSTATIC -> emitPutStatic(translatedOwner, translatedMember, translatedDesc, tempVar)
        }
    }

    /** 生成方法调用（INVOKEVIRTUAL / INVOKEINTERFACE / INVOKESTATIC） */
    private fun emitInvoke(asmOpcode: Int, owner: String, method: String, desc: String, tempVar: Int, hasInstance: Boolean) {
        val methodType = Type.getMethodType(desc)
        val argTypes = methodType.argumentTypes
        val returnType = methodType.returnType
        var argIndex = 0
        if (hasInstance) {
            emitLoadArg(tempVar, argIndex, Type.getObjectType(owner))
            argIndex++
        }
        for (paramType in argTypes) {
            emitLoadArg(tempVar, argIndex, paramType)
            argIndex++
        }
        val isInterface = hasInstance && isInterfaceClass(owner)
        val actualOpcode = if (isInterface) Opcodes.INVOKEINTERFACE else asmOpcode
        mv.visitMethodInsn(actualOpcode, owner, method, desc, isInterface)
        boxOrNullReturn(returnType)
    }

    /** 生成构造函数调用 */
    private fun emitInvokeSpecial(owner: String, desc: String, tempVar: Int) {
        val methodType = Type.getMethodType(desc)
        val argTypes = methodType.argumentTypes
        mv.visitTypeInsn(Opcodes.NEW, owner)
        mv.visitInsn(Opcodes.DUP)
        for (i in argTypes.indices) {
            emitLoadArg(tempVar, i, argTypes[i])
        }
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, "<init>", desc, false)
    }

    /** 生成 GETFIELD */
    private fun emitGetField(owner: String, field: String, desc: String, tempVar: Int) {
        val fieldType = Type.getType(desc)
        emitLoadArg(tempVar, 0, Type.getObjectType(owner))
        mv.visitFieldInsn(Opcodes.GETFIELD, owner, field, desc)
        boxReturnValue(fieldType)
    }

    /** 生成 PUTFIELD */
    private fun emitPutField(owner: String, field: String, desc: String, tempVar: Int) {
        val fieldType = Type.getType(desc)
        emitLoadArg(tempVar, 0, Type.getObjectType(owner))
        emitLoadArg(tempVar, 1, fieldType)
        mv.visitFieldInsn(Opcodes.PUTFIELD, owner, field, desc)
        mv.visitInsn(Opcodes.ACONST_NULL)
    }

    /** 生成 GETSTATIC */
    private fun emitGetStatic(owner: String, field: String, desc: String, tempVar: Int) {
        val fieldType = Type.getType(desc)
        mv.visitFieldInsn(Opcodes.GETSTATIC, owner, field, desc)
        boxReturnValue(fieldType)
    }

    /** 生成 PUTSTATIC */
    private fun emitPutStatic(owner: String, field: String, desc: String, tempVar: Int) {
        val fieldType = Type.getType(desc)
        emitLoadArg(tempVar, 0, fieldType)
        mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, field, desc)
        mv.visitInsn(Opcodes.ACONST_NULL)
    }

    /** 从 Object[] 临时变量中加载第 index 个参数，并执行类型转换/拆箱 */
    private fun emitLoadArg(tempVar: Int, index: Int, targetType: Type) {
        mv.visitVarInsn(Opcodes.ALOAD, tempVar)
        pushInt(index)
        mv.visitInsn(Opcodes.AALOAD)
        when (targetType.sort) {
            Type.BOOLEAN -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false)
            }
            Type.BYTE -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false)
            }
            Type.CHAR -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false)
            }
            Type.SHORT -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false)
            }
            Type.INT -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
            }
            Type.LONG -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false)
            }
            Type.FLOAT -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false)
            }
            Type.DOUBLE -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false)
            }
            Type.OBJECT, Type.ARRAY -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, targetType.internalName)
            }
        }
    }

    /** 对方法返回值进行装箱或生成 null（void） */
    private fun boxOrNullReturn(returnType: Type) {
        if (returnType.sort == Type.VOID) {
            mv.visitInsn(Opcodes.ACONST_NULL)
        } else {
            boxReturnValue(returnType)
        }
    }

    /** 对原始类型返回值进行装箱 */
    private fun boxReturnValue(type: Type) {
        when (type.sort) {
            Type.BOOLEAN -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false)
            Type.BYTE -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false)
            Type.CHAR -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false)
            Type.SHORT -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false)
            Type.INT -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
            Type.LONG -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false)
            Type.FLOAT -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false)
            Type.DOUBLE -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false)
        }
    }

    /** 生成 push int 指令 */
    private fun pushInt(value: Int) {
        when (value) {
            in -1..5 -> mv.visitInsn(Opcodes.ICONST_0 + value)
            in Byte.MIN_VALUE..Byte.MAX_VALUE -> mv.visitIntInsn(Opcodes.BIPUSH, value)
            in Short.MIN_VALUE..Short.MAX_VALUE -> mv.visitIntInsn(Opcodes.SIPUSH, value)
            else -> mv.visitLdcInsn(value)
        }
    }

    /** 翻译描述符中的类引用 */
    private fun translateDescriptor(descriptor: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < descriptor.length) {
            when (descriptor[i]) {
                'L' -> {
                    val end = descriptor.indexOf(';', i)
                    val className = descriptor.substring(i + 1, end)
                    val translated = remapTranslation.translate(className)
                    sb.append('L').append(translated).append(';')
                    i = end + 1
                }
                '[' -> {
                    sb.append('[')
                    i++
                }
                else -> {
                    sb.append(descriptor[i])
                    i++
                }
            }
        }
        return sb.toString()
    }

    /**
     * 将用户描述符规范化为合法 JVM 格式
     *
     * 字段描述符（以下写法均可）：
     * - `I` / `Z` 等原始类型
     * - `net.minecraft.core.RegistryBlocks`
     * - `net.minecraft.core.RegistryBlocks;`
     * - `Lnet.minecraft.core.RegistryBlocks;`
     * - `Lnet/minecraft/core/RegistryBlocks;`
     *
     * 方法描述符（允许省略 `L`，用 `;` 分隔类引用）：
     * - `(java.lang.Object;)V` → `(Ljava/lang/Object;)V`
     * - `(Ljava.lang.Object;)V` → `(Ljava/lang/Object;)V`
     * - `(IF)V` → `(IF)V`
     */
    private fun normalizeDescriptor(descriptor: String, isField: Boolean): String {
        if (isField) {
            return normalizeFieldDescriptor(descriptor)
        }
        return normalizeMethodDescriptor(descriptor)
    }

    private fun normalizeFieldDescriptor(descriptor: String): String {
        // 去除尾部多余的分号和空白
        val d = descriptor.trim().removeSuffix(";")
        // 原始类型
        if (d.length == 1 && d[0] in "ZBCSIJFDV") {
            return d
        }
        // 已有 L 前缀（去掉尾部 ; 后不会再有 ;）
        if (d.startsWith("L")) {
            return "L" + d.substring(1).replace('.', '/') + ";"
        }
        // 数组
        if (d.startsWith("[")) {
            return "[" + normalizeFieldDescriptor(d.substring(1))
        }
        // 纯类名
        return "L" + d.replace('.', '/') + ";"
    }

    private fun normalizeMethodDescriptor(descriptor: String): String {
        val sb = StringBuilder()
        var i = 0
        val len = descriptor.length
        while (i < len) {
            when (val c = descriptor[i]) {
                in "()" -> {
                    sb.append(c)
                    i++
                }
                '[' -> {
                    sb.append('[')
                    i++
                }
                'L' -> {
                    // 显式 L...;，替换点号为斜杠
                    val end = descriptor.indexOf(';', i)
                    check(end > i) { "方法描述符格式错误，缺少 ';': ${descriptor.substring(i)}" }
                    sb.append('L').append(descriptor.substring(i + 1, end).replace('.', '/')).append(';')
                    i = end + 1
                }
                in "ZBCSIJFDV" -> {
                    // 可能是原始类型，也可能是省略 L 的类名（如 Vector3f;）
                    // 判断依据：后续是否有 ; 出现在下一个 ) 或类型边界之前
                    val nextSemicolon = descriptor.indexOf(';', i)
                    val nextParen = descriptor.indexOf(')', i)
                    if (nextSemicolon != -1 && (nextParen == -1 || nextSemicolon < nextParen)) {
                        // 省略了 L 的类名
                        sb.append('L').append(descriptor.substring(i, nextSemicolon).replace('.', '/')).append(';')
                        i = nextSemicolon + 1
                    } else {
                        // 原始类型
                        sb.append(c)
                        i++
                    }
                }
                else -> {
                    // 省略了 L 的类名（小写字母开头，如 java.lang.Object;）
                    val end = descriptor.indexOf(';', i)
                    check(end > i) { "方法描述符格式错误，缺少 ';': ${descriptor.substring(i)}" }
                    sb.append('L').append(descriptor.substring(i, end).replace('.', '/')).append(';')
                    i = end + 1
                }
            }
        }
        return sb.toString()
    }

    /** 判断目标类是否为 interface */
    private fun isInterfaceClass(internalName: String): Boolean {
        return try {
            ClassHelper.getClass(internalName.replace('/', '.')).isInterface
        } catch (_: Throwable) {
            false
        }
    }

    /** mapMethodName/mapFieldName 容错 */
    private fun tryMapName(block: () -> String): String? {
        return try {
            block()
        } catch (_: Throwable) {
            null
        }
    }

    companion object {

        private const val DYNAMIC_OPCODE_OWNER = "taboolib/module/nms/remap/DynamicOpcode"
        private const val DYNAMIC_KT_OWNER = "taboolib/module/nms/remap/RemapDynamicKt"

        private val dynamicOpcodeDesc: String
            get() = "L$DYNAMIC_OPCODE_OWNER;"

        private val dynamicMethodDesc: String
            get() = "(${dynamicOpcodeDesc}Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"

        /**
         * 解析用户描述符
         *
         * 方法：`类名#方法名(参数描述符)返回描述符`
         * 构造：`类名(参数描述符)V`
         * 字段：`类名#字段名:字段描述符`
         */
        fun parseDescriptor(opcode: DynamicOpcode, raw: String): ParsedDescriptor {
            return when (opcode) {
                DynamicOpcode.INVOKESPECIAL -> {
                    val parenIdx = raw.indexOf('(')
                    check(parenIdx > 0) { "构造函数描述符缺少 '(': $raw" }
                    ParsedDescriptor(raw.take(parenIdx), null, raw.substring(parenIdx), isConstructor = true, isField = false)
                }
                DynamicOpcode.GETFIELD, DynamicOpcode.PUTFIELD, DynamicOpcode.GETSTATIC, DynamicOpcode.PUTSTATIC -> {
                    val hashIdx = raw.indexOf('#')
                    check(hashIdx > 0) { "字段描述符缺少 '#': $raw" }
                    val colonIdx = raw.indexOf(':', hashIdx)
                    check(colonIdx > hashIdx) { "字段描述符缺少 ':': $raw" }
                    ParsedDescriptor(raw.take(hashIdx), raw.substring(hashIdx + 1, colonIdx), raw.substring(colonIdx + 1), isConstructor = false, isField = true)
                }
                else -> {
                    val hashIdx = raw.indexOf('#')
                    check(hashIdx > 0) { "方法描述符缺少 '#': $raw" }
                    val parenIdx = raw.indexOf('(', hashIdx)
                    check(parenIdx > hashIdx) { "方法描述符缺少 '(': $raw" }
                    ParsedDescriptor(raw.take(hashIdx), raw.substring(hashIdx + 1, parenIdx), raw.substring(parenIdx), isConstructor = false, isField = false)
                }
            }
        }
    }

    data class ParsedDescriptor(
        val className: String,
        val memberName: String?,
        val memberDescriptor: String,
        val isConstructor: Boolean,
        val isField: Boolean
    )
}
