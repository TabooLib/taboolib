package com.ilummc.eagletdl;

public class StartEvent {

    private EagletTask task;

    StartEvent(EagletTask task) {
        this.task = task;
    }

    public EagletTask getTask() {
        return task;
    }
}
