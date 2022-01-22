@file:Isolated
package taboolib.platform.util

import org.bukkit.Bukkit
import taboolib.common.Isolated
import org.tabooproject.reflex.Reflex.Companion.getProperty

val isBukkitServerRunning: Boolean
    get() {
        return try {
            !Bukkit.getServer().getProperty<Boolean>("console/stopped")!!
        } catch (ex: NoSuchFieldException) {
            !Bukkit.getServer().getProperty<Boolean>("console/hasStopped")!!
        }
    }