package taboolib.module.incision.annotation

import org.objectweb.asm.Opcodes

/**
 * ASM 字节码指令枚举。
 *
 * 用途：
 * 为 [Step.opcode] 提供稳定、可读的枚举值，避免在模式里直接手写 ASM 整数常量。
 *
 * 使用：
 * 大多数情况下直接在 `Step(opcode = Op.INVOKEVIRTUAL)` 里引用即可；
 * 当需要从整数 opcode 反查时，可使用 [fromInt]。
 *
 * 效果：
 * 该枚举基本与 [org.objectweb.asm.Opcodes] 一一对应，`ANY` 额外表示“任意指令”。
 *
 * 局限：
 * 1. 它只表达 opcode 类型，不携带 owner/name/desc 等额外条件。
 * 2. 如果底层指令值来自 ASM 之外的特殊语义，仍需结合实际字节码理解。
 *
 * @property opcode 对应的 ASM 指令值；`-1` 为通配（[ANY]）。
 */
enum class Op(val opcode: Int) {

    /** 通配：匹配任意指令。 */
    ANY(-1),

    NOP(Opcodes.NOP),
    ACONST_NULL(Opcodes.ACONST_NULL),
    ICONST_M1(Opcodes.ICONST_M1),
    ICONST_0(Opcodes.ICONST_0),
    ICONST_1(Opcodes.ICONST_1),
    ICONST_2(Opcodes.ICONST_2),
    ICONST_3(Opcodes.ICONST_3),
    ICONST_4(Opcodes.ICONST_4),
    ICONST_5(Opcodes.ICONST_5),
    LCONST_0(Opcodes.LCONST_0),
    LCONST_1(Opcodes.LCONST_1),
    FCONST_0(Opcodes.FCONST_0),
    FCONST_1(Opcodes.FCONST_1),
    FCONST_2(Opcodes.FCONST_2),
    DCONST_0(Opcodes.DCONST_0),
    DCONST_1(Opcodes.DCONST_1),
    BIPUSH(Opcodes.BIPUSH),
    SIPUSH(Opcodes.SIPUSH),
    LDC(Opcodes.LDC),

    ILOAD(Opcodes.ILOAD),
    LLOAD(Opcodes.LLOAD),
    FLOAD(Opcodes.FLOAD),
    DLOAD(Opcodes.DLOAD),
    ALOAD(Opcodes.ALOAD),

    IALOAD(Opcodes.IALOAD),
    LALOAD(Opcodes.LALOAD),
    FALOAD(Opcodes.FALOAD),
    DALOAD(Opcodes.DALOAD),
    AALOAD(Opcodes.AALOAD),
    BALOAD(Opcodes.BALOAD),
    CALOAD(Opcodes.CALOAD),
    SALOAD(Opcodes.SALOAD),

    ISTORE(Opcodes.ISTORE),
    LSTORE(Opcodes.LSTORE),
    FSTORE(Opcodes.FSTORE),
    DSTORE(Opcodes.DSTORE),
    ASTORE(Opcodes.ASTORE),

    IASTORE(Opcodes.IASTORE),
    LASTORE(Opcodes.LASTORE),
    FASTORE(Opcodes.FASTORE),
    DASTORE(Opcodes.DASTORE),
    AASTORE(Opcodes.AASTORE),
    BASTORE(Opcodes.BASTORE),
    CASTORE(Opcodes.CASTORE),
    SASTORE(Opcodes.SASTORE),

    POP(Opcodes.POP),
    POP2(Opcodes.POP2),
    DUP(Opcodes.DUP),
    DUP_X1(Opcodes.DUP_X1),
    DUP_X2(Opcodes.DUP_X2),
    DUP2(Opcodes.DUP2),
    DUP2_X1(Opcodes.DUP2_X1),
    DUP2_X2(Opcodes.DUP2_X2),
    SWAP(Opcodes.SWAP),

    IADD(Opcodes.IADD),
    LADD(Opcodes.LADD),
    FADD(Opcodes.FADD),
    DADD(Opcodes.DADD),
    ISUB(Opcodes.ISUB),
    LSUB(Opcodes.LSUB),
    FSUB(Opcodes.FSUB),
    DSUB(Opcodes.DSUB),
    IMUL(Opcodes.IMUL),
    LMUL(Opcodes.LMUL),
    FMUL(Opcodes.FMUL),
    DMUL(Opcodes.DMUL),
    IDIV(Opcodes.IDIV),
    LDIV(Opcodes.LDIV),
    FDIV(Opcodes.FDIV),
    DDIV(Opcodes.DDIV),
    IREM(Opcodes.IREM),
    LREM(Opcodes.LREM),
    FREM(Opcodes.FREM),
    DREM(Opcodes.DREM),
    INEG(Opcodes.INEG),
    LNEG(Opcodes.LNEG),
    FNEG(Opcodes.FNEG),
    DNEG(Opcodes.DNEG),
    ISHL(Opcodes.ISHL),
    LSHL(Opcodes.LSHL),
    ISHR(Opcodes.ISHR),
    LSHR(Opcodes.LSHR),
    IUSHR(Opcodes.IUSHR),
    LUSHR(Opcodes.LUSHR),
    IAND(Opcodes.IAND),
    LAND(Opcodes.LAND),
    IOR(Opcodes.IOR),
    LOR(Opcodes.LOR),
    IXOR(Opcodes.IXOR),
    LXOR(Opcodes.LXOR),
    IINC(Opcodes.IINC),

    I2L(Opcodes.I2L),
    I2F(Opcodes.I2F),
    I2D(Opcodes.I2D),
    L2I(Opcodes.L2I),
    L2F(Opcodes.L2F),
    L2D(Opcodes.L2D),
    F2I(Opcodes.F2I),
    F2L(Opcodes.F2L),
    F2D(Opcodes.F2D),
    D2I(Opcodes.D2I),
    D2L(Opcodes.D2L),
    D2F(Opcodes.D2F),
    I2B(Opcodes.I2B),
    I2C(Opcodes.I2C),
    I2S(Opcodes.I2S),

    LCMP(Opcodes.LCMP),
    FCMPL(Opcodes.FCMPL),
    FCMPG(Opcodes.FCMPG),
    DCMPL(Opcodes.DCMPL),
    DCMPG(Opcodes.DCMPG),

    IFEQ(Opcodes.IFEQ),
    IFNE(Opcodes.IFNE),
    IFLT(Opcodes.IFLT),
    IFGE(Opcodes.IFGE),
    IFGT(Opcodes.IFGT),
    IFLE(Opcodes.IFLE),
    IF_ICMPEQ(Opcodes.IF_ICMPEQ),
    IF_ICMPNE(Opcodes.IF_ICMPNE),
    IF_ICMPLT(Opcodes.IF_ICMPLT),
    IF_ICMPGE(Opcodes.IF_ICMPGE),
    IF_ICMPGT(Opcodes.IF_ICMPGT),
    IF_ICMPLE(Opcodes.IF_ICMPLE),
    IF_ACMPEQ(Opcodes.IF_ACMPEQ),
    IF_ACMPNE(Opcodes.IF_ACMPNE),

    GOTO(Opcodes.GOTO),
    JSR(Opcodes.JSR),
    RET(Opcodes.RET),
    TABLESWITCH(Opcodes.TABLESWITCH),
    LOOKUPSWITCH(Opcodes.LOOKUPSWITCH),

    IRETURN(Opcodes.IRETURN),
    LRETURN(Opcodes.LRETURN),
    FRETURN(Opcodes.FRETURN),
    DRETURN(Opcodes.DRETURN),
    ARETURN(Opcodes.ARETURN),
    RETURN(Opcodes.RETURN),

    GETSTATIC(Opcodes.GETSTATIC),
    PUTSTATIC(Opcodes.PUTSTATIC),
    GETFIELD(Opcodes.GETFIELD),
    PUTFIELD(Opcodes.PUTFIELD),

    INVOKEVIRTUAL(Opcodes.INVOKEVIRTUAL),
    INVOKESPECIAL(Opcodes.INVOKESPECIAL),
    INVOKESTATIC(Opcodes.INVOKESTATIC),
    INVOKEINTERFACE(Opcodes.INVOKEINTERFACE),
    INVOKEDYNAMIC(Opcodes.INVOKEDYNAMIC),

    NEW(Opcodes.NEW),
    NEWARRAY(Opcodes.NEWARRAY),
    ANEWARRAY(Opcodes.ANEWARRAY),
    ARRAYLENGTH(Opcodes.ARRAYLENGTH),
    ATHROW(Opcodes.ATHROW),
    CHECKCAST(Opcodes.CHECKCAST),
    INSTANCEOF(Opcodes.INSTANCEOF),
    MONITORENTER(Opcodes.MONITORENTER),
    MONITOREXIT(Opcodes.MONITOREXIT),
    MULTIANEWARRAY(Opcodes.MULTIANEWARRAY),
    IFNULL(Opcodes.IFNULL),
    IFNONNULL(Opcodes.IFNONNULL);

    companion object {

        private val byOpcode: Map<Int, Op> = values().associateBy { it.opcode }

        /** 按 ASM 指令值反查枚举；未知值返回 `null`，通常用于调试或诊断输出。 */
        @JvmStatic
        fun fromInt(v: Int): Op? = byOpcode[v]
    }
}
