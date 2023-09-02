package taboolib.expansion.fx

import org.bukkit.Location
import org.bukkit.Material
import kotlin.math.cos
import kotlin.math.sin

/** 取一个Location 周围获得一个 空气位置的随机点 */
fun getRandomLocationNoAir(location: Location, radius: Double): Location {
    var locations = getRandomLocation(location, radius)
    while (locations.block.type != Material.AIR) {
        locations = getRandomLocation(location, radius)
    }
    return locations
}

/** 取一个Location 周围获得一个 随机点 */
fun getRandomLocation(location: Location, radius: Double): Location {
    val radians = Math.toRadians((0..360).random().toDouble())
    val x = cos(radians) * radius
    val z = sin(radians) * radius
    return location.add(x, 1.0, z)
}
