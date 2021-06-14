package taboolib.common.platform.io

import taboolib.common.platform.PlatformFactory.platformIO

fun info(vararg message: Any?) = platformIO.info(*message)

fun severe(vararg message: Any?) = platformIO.severe(*message)

fun warning(vararg message: Any?) = platformIO.warning(*message)

fun releaseResourceFile(path: String, replace: Boolean = false) = platformIO.releaseResourceFile(path, replace)

fun getJarFile() = platformIO.getJarFile()