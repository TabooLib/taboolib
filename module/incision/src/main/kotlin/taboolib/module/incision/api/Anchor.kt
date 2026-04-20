package taboolib.module.incision.api

/**
 * 锚点类型 — 描述在目标方法中的插入位置。
 */
enum class Anchor {
    /** 方法入口 */
    HEAD,

    /** 方法所有正常出口（每个 return 前） */
    TAIL,

    /** 方法的 return 指令（可含返回值修改） */
    RETURN,

    /** 方法调用指令处（INVOKEVIRTUAL / INVOKESTATIC / INVOKESPECIAL / INVOKEINTERFACE） */
    INVOKE,

    /** 字段读取 (GETFIELD / GETSTATIC) */
    FIELD_GET,

    /** 字段写入 (PUTFIELD / PUTSTATIC) */
    FIELD_PUT,

    /** new 指令（对象构造） */
    NEW,

    /** throw 指令 */
    THROW,
}
