package taboolib.module.multiblocks

/**
 * 多方块结构的旋转方向（绕 Y 轴）
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
enum class MultiblockRotation {

    /** 不旋转 */
    NONE,

    /** 顺时针 90°（俯视） */
    CLOCKWISE_90,

    /** 旋转 180° */
    CLOCKWISE_180,

    /** 逆时针 90°（俯视） */
    COUNTERCLOCKWISE_90;

    companion object {

        /** 所有旋转方向 */
        val ALL = values().toList()
    }
}
