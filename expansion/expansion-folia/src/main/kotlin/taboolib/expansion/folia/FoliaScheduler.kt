package taboolib.expansion.folia

fun submitFolia(delay: Long = 0, period: Long = 0, action: FoliaTaskCallBack.() -> Unit): FoliaTaskCallBack {
    val scheduler = Folia.api.getScheduler()
    val back = FoliaTaskCallBack()
    back.scheduler = scheduler
    if (period > 0) {
        scheduler.runTaskTimer({
            action.invoke(back)
        }, delay, period).apply {
            back.task = this
        }
    }
    if (delay <= 0) {
        scheduler.runTask {
            action.invoke(back)
        }.apply {
            back.task = this
        }
    } else {
        scheduler.runTaskLater({
            action.invoke(back)
        }, delay).apply {
            back.task = this
        }
    }
    return back
}
