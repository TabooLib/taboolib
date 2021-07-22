package taboolib.test

import taboolib.common.TabooLibCommon
import taboolib.common.platform.CommandBuilder
import taboolib.module.configuration.ConfigLoader

object Test2 {

    fun test(func: CommandBuilder.CommandBase.() -> Unit) {
        println(CommandBuilder.CommandBase().also(func))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        TabooLibCommon.ENV.inject(ConfigLoader::class.java)


        test {
            literal("demo") {
                literal("demo2", optional = true) {
                    execute { context, argument ->

                    }
                }
                execute { context, argument ->

                }
            }
            execute { context, argument ->

            }
        }
    }
}