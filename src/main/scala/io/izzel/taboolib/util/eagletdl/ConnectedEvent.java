package io.izzel.taboolib.util.eagletdl;

public class ConnectedEvent {

    private long contentLength;
    private EagletTask task;

    public ConnectedEvent(long length, EagletTask task) {
        this.contentLength = length;
        this.task = task;
    }

    /**
     * Get the length of the download task.
     * <p>
     * If the length is -1, this task cannot be downloaded in multiple threads.
     *
     * @return length
     */
    public long getContentLength() {
        return contentLength;
    }

    public EagletTask getTask() {
        return task;
    }
}
