package taboolib.common.platform

import taboolib.common.io.Isolated

/**
 * TabooLib
 * taboolib.module.command.CommandBox
 *
 * @author sky
 * @since 2021/6/25 9:59 上午
 */
class CommandBox<T>(var value: T) {

    override fun toString(): String {
        return value.toString()
    }
}