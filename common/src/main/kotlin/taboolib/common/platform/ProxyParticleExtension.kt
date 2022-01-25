@file:Isolated

package taboolib.common.platform

import taboolib.common.Isolated
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.Location
import taboolib.common.util.Vector

fun ProxyParticle.sendTo(
    player: ProxyPlayer,
    location: Location,
    offset: Vector = Vector(0, 0, 0),
    count: Int = 1,
    speed: Double = 0.0,
    data: ProxyParticle.Data? = null,
) {
    player.sendParticle(this, location, offset, count, speed, data)
}

fun ProxyParticle.sendTo(
    location: Location,
    range: Double = 128.0,
    offset: Vector = Vector(0, 0, 0),
    count: Int = 1,
    speed: Double = 0.0,
    data: ProxyParticle.Data? = null,
) {
    onlinePlayers().filter { it.world == location.world && it.location.distance(location) <= range }.forEach { sendTo(it, location, offset, count, speed, data) }
}