package taboolib.module.kether;

import io.izzel.kether.common.util.LocalizedException;

public enum KetherError {

    NOT_SYMBOL,
    NOT_COMMAND_SENDER,
    NOT_EVENT_OPERATOR,
    NOT_EVENT,
    NOT_PLAYER_OPERATOR,
    NOT_MATERIAL,
    CUSTOM;

    public LocalizedException create(Object... args) {
        return LocalizedException.of("load-error." + name().toLowerCase().replace("_", "-"), args);
    }
}
