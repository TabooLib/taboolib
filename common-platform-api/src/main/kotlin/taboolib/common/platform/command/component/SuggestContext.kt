package taboolib.common.platform.command.component

import taboolib.common.platform.command.CommandContext

/**
 * TabooLib
 * taboolib.common.platform.command.component.SuggestContext
 *
 * @author 坏黑
 * @since 2024/3/24 15:42
 */
data class SuggestContext<T>(val sender: T, val ctx: CommandContext<T>)