package taboolib.module.kether

import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import java.util.concurrent.Executor

object ScriptSchedulerExecutor : Executor {

    override fun execute(command: Runnable) {
        if (isPrimaryThread) {
            command.run()
        } else {
            submit { command.run() }
        }
    }
}