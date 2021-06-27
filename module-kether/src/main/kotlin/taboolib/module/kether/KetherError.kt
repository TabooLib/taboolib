package taboolib.module.kether

import io.izzel.kether.common.util.LocalizedException

enum class KetherError {

    NOT_SYMBOL, NOT_COMMAND_SENDER, NOT_EVENT_OPERATOR, NOT_EVENT, NOT_PLAYER_OPERATOR, NOT_MATERIAL, CUSTOM;

    fun create(vararg args: Any?): LocalizedException {
        return LocalizedException.of("load-error." + name.toLowerCase().replace("_", "-"), *args)
    }
}