package taboolib.module.navigation

/**
 * Navigation
 * taboolib.module.navigation.PathType
 * net.minecraft.world.level.pathfinder.PathType
 *
 * @author sky
 * @since 2021/2/21 6:35 下午
 */
enum class PathType(val malus: Float) {

    OPEN(0.0f),
    BLOCKED(-1.0f),

    WALKABLE(0.0f),
    WALKABLE_DOOR(0.0f),
    TRAPDOOR(0.0f),
    FENCE(-1.0f),

    LAVA(-1.0f),
    WATER(8.0f),
    WATER_BORDER(8.0f),

    RAIL(0.0f),
    UNPASSABLE_RAIL(-1.0f),

    DAMAGE_FIRE(16.0f),
    DAMAGE_CACTUS(-1.0f),
    DAMAGE_OTHER(-1.0f),

    DANGER_FIRE(8.0f),
    DANGER_CACTUS(8.0f),
    DANGER_OTHER(8.0f),

    DOOR_OPEN(0.0f),
    DOOR_WOOD_CLOSED(-1.0f),
    DOOR_IRON_CLOSED(-1.0f),

    BREACH(4.0f),
    LEAVES(-1.0f),

    STICKY_HONEY(8.0f),
    COCOA(0.0f),
}