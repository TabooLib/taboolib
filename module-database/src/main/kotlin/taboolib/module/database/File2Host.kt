@file:Isolated

package taboolib.module.database

import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.SecuredFile
import java.io.File

fun File.getHost(): Host {
    return HostSQLite(this)
}

fun ConfigurationSection.getHost(name: String): Host {
    return HostSQL(getConfigurationSection(name) ?: SecuredFile())
}