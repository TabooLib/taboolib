package com.ilummc.eagletdl;

public class RetryFailedException extends RuntimeException {

    private EagletTask task;

    RetryFailedException(EagletTask task) {
        this.task = task;
    }

    public EagletTask getTask() {
        return task;
    }
}
