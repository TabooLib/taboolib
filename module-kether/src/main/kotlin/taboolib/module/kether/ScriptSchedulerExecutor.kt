package taboolib.module.kether

import taboolib.common.platform.isPrimaryThread
import java.util.concurrent.Executor

object ScriptSchedulerExecutor : Executor {

    override fun execute(command: Runnable) {
        if (isPrimaryThread) {
            command.run()
        } else {
            execute { command.run() }
        }
    }
}