package taboolib.platform.util

/**
 * 表示移动方向的枚举类。
 * 作为 [PlayerMoveEvent#moveDirection] 的返回值使用。
 */
enum class MoveDirection {

    /** 表示向前移动 */
    FORWARD,

    /** 表示向后移动 */
    BACKWARD,

    /** 表示向左移动 */
    LEFT,

    /** 表示向右移动 */
    RIGHT
}