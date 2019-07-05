package io.izzel.taboolib.origin.plugin;

/**
 * @Author sky
 * @Since 2018-06-01 21:34
 */
public class PluginLoadState {

    private final PluginLoadStateType stateType;
    private final String message;

    public PluginLoadState(PluginLoadStateType stateType, String message) {
        this.stateType = stateType;
        this.message = message;
    }

    public PluginLoadStateType getStateType() {
        return stateType;
    }

    public String getMessage() {
        return message;
    }
}
