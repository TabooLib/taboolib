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
        // npc create [id] [type]
        // npc tp <id>
        command("npc") {
            literal("create") {
                required {
                    val id = argument
                    required {
                        val type = id + argument
                        // API.createNPC(id, argument)
                    }
                }
            }
            literal("tp") {
                fun teleport() {
                    // npc.teleport(xx)
                }
                required {
                    val id = argument
                    // API.getNPC(id)
                    teleport()
                }
                optional {
                    // API.getNearNPC()
                    teleport()
                }
            }
        }
        // test demo1
        // 1
        command("test") {
            literal("demo1") {
                literal("demo2") {

                }
            }
            required {

            }
            optional {
                complete { onlinePlayers().map { it.name } }
                restrict { startsWith("demo") }
                optional {
                    optional {

                    }
                }
            }
            execute {

            }
        }
    }
}