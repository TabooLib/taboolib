package io.izzel.taboolib.util.plugin;

/**
 * @Author sky
 * @Since 2018-06-01 21:39
 */
public class PluginUnloadState {

    private final boolean failed;
    private final String message;

    public PluginUnloadState(boolean failed, String message) {
        this.failed = failed;
        this.message = message;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getMessage() {
        return message;
    }
}
