package taboolib.module.command

import taboolib.common.platform.onlinePlayers

/**
 * TabooLib
 * taboolib.module.command.Test
 *
 * @author sky
 * @since 2021/6/25 12:51 上午
 */
class Test {

    init {
        command("test") {
            literal("demo") {
            }
            optional {
                complete { onlinePlayers().map { it.name } }
                restrict { startsWith("demo") }
                optional {
                    optional {

                    }
                }
            }
        }
    }
}