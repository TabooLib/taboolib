package io.izzel.taboolib.util.eagletdl;

public class StartEvent {

    private EagletTask task;

    StartEvent(EagletTask task) {
        this.task = task;
    }

    public EagletTask getTask() {
        return task;
    }
}
