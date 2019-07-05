package io.izzel.taboolib.util.eagletdl;

public class CompleteEvent {
    private EagletTask task;
    private boolean success;

    CompleteEvent(EagletTask task, boolean success) {
        this.task = task;
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public EagletTask getTask() {
        return task;
    }
}
