package taboolib.expansion

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import taboolib.module.lang.sendWarn

internal fun infoMessageAsLang(node: String) {
    console().sendInfo(node)
}

internal fun infoMessageAsLang(node: String, vararg args: Any) {
    console().sendInfo(node, *args)
}

internal fun warningMessageAsLang(node: String) {
    console().sendWarn(node)
}

internal fun warningMessageAsLang(node: String, vararg args: Any) {
    console().sendWarn(node, *args)
}

internal fun ProxyCommandSender.infoAsLang(node: String) {
    if (this is ProxyPlayer) {
        this.sendInfo(node)
    } else {
        infoMessageAsLang(node)
    }
}

internal fun ProxyCommandSender.infoAsLang(node: String, vararg args: Any) {
    if (this is ProxyPlayer) {
        this.sendInfo(node, *args)
    } else {
        infoMessageAsLang(node, *args)
    }
}

internal fun ProxyCommandSender.warningAsLang(node: String) {
    if (this is ProxyPlayer) {
        this.sendWarn(node)
    } else {
        warningMessageAsLang(node)
    }
}

internal fun ProxyCommandSender.warningAsLang(node: String, vararg args: Any) {
    if (this is ProxyPlayer) {
        this.sendWarn(node, *args)
    } else {
        warningMessageAsLang(node, *args)
    }
}