package taboolib.expansion.folia

import taboolib.expansion.folialib.Wrapper.Scheduler
import taboolib.expansion.folialib.Wrapper.Task

data class FoliaTaskCallBack(
        var task: Task? = null,
        var scheduler: Scheduler? = null
) {

    fun cancel() {
        task?.cancel()
    }

}
