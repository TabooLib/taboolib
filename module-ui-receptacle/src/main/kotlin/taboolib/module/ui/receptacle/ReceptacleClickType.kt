package taboolib.module.ui.receptacle

/**
 * @author Arasple
 * @date 2020/12/5 22:01
 */
enum class ReceptacleClickType(private val mode: Int, private val button: Int) {

    ALL(-1, -1),

    LEFT(0, 0),

    RIGHT(0, 1),

    SHIFT_LEFT(1, 0),

    SHIFT_RIGHT(1, 1),

    OFFHAND(2, 40),

    NUMBER_KEY(2, -1),

    NUMBER_KEY_1(2, 0),

    NUMBER_KEY_2(2, 1),

    NUMBER_KEY_3(2, 2),

    NUMBER_KEY_4(2, 3),

    NUMBER_KEY_5(2, 4),

    NUMBER_KEY_6(2, 5),

    NUMBER_KEY_7(2, 6),

    NUMBER_KEY_8(2, 7),

    NUMBER_KEY_9(2, 8),

    MIDDLE(3, 2),

    // clicked Item will be empty
    DROP(4, 0),

    CONTROL_DROP(4, 1),

    ABROAD_LEFT_EMPTY(4, 0),

    ABROAD_RIGHT_EMPTY(4, 1),

    ABROAD_LEFT_ITEM(0, 0),

    ABROAD_RIGHT_ITEM(0, 1),

    LEFT_MOUSE_DRAG_ADD(5, 1),

    RIGHT_MOUSE_DRAG_ADD(5, 5),

    MIDDLE_MOUSE_DRAG_ADD(5, 9),

    DOUBLE_CLICK(6, 0),

    UNKNOWN(-1, -1);

    fun equals(mode: Int, button: Int): Boolean {
        return this.mode == mode && this.button == button
    }

    fun isRightClick(): Boolean {
        return this == RIGHT || this == SHIFT_RIGHT
    }

    fun isLeftClick(): Boolean {
        return this == LEFT || this == SHIFT_LEFT || this == DOUBLE_CLICK
    }

    fun isShiftClick(): Boolean {
        return this == SHIFT_LEFT || this == SHIFT_RIGHT
    }

    fun isKeyboardClick(): Boolean {
        return isNumberKeyClick() || this == DROP || this == CONTROL_DROP
    }

    fun isNumberKeyClick(): Boolean {
        return this.name.startsWith("NUMBER_KEY") || this == OFFHAND
    }

    fun isDoubleClick(): Boolean {
        return this == DOUBLE_CLICK
    }

    fun isCreativeAction(): Boolean {
        return this == MIDDLE || this == MIDDLE_MOUSE_DRAG_ADD
    }

    fun isItemMoveable(): Boolean {
        return isKeyboardClick() || isShiftClick() || isCreativeAction() || isDoubleClick()
    }

    companion object {

        private val modes = arrayOf("PICKUP", "QUICK_MOVE", "SWAP", "CLONE", "THROW", "QUICK_CRAFT", "PICKUP_ALL")

        fun matchesFirst(string: String): ReceptacleClickType {
            return values().find { it.name.equals(string, true) } ?: ALL
        }

        fun matches(string: String): Set<ReceptacleClickType> {
            return string.split(",", ";").map { matchesFirst(it) }.toSet()
        }

        fun from(mode: String, button: Int, slot: Int = -1): ReceptacleClickType? {
            return from(modes.indexOf(mode), button, slot)
        }

        fun from(mode: Int, button: Int, slot: Int = -1): ReceptacleClickType? {
            if (slot == -999) {
                return when {
                    LEFT.equals(mode, button) -> ABROAD_LEFT_ITEM
                    RIGHT.equals(mode, button) -> ABROAD_RIGHT_ITEM
                    ABROAD_LEFT_EMPTY.equals(mode, button) -> ABROAD_LEFT_EMPTY
                    ABROAD_RIGHT_EMPTY.equals(mode, button) -> ABROAD_RIGHT_EMPTY
                    else -> UNKNOWN
                }
            }
            return values().find { it.equals(mode, button) }
        }
    }
}