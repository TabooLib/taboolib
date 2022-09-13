package taboolib.module.nms

import org.bukkit.Location
import org.bukkit.entity.Entity
import java.util.function.Consumer

/**
 * 生成实体并在生成之前执行特定行为
 */
fun <T : Entity> Location.spawnEntity(entity: Class<T>, func: Consumer<T>) {
    nmsProxy<NMSGeneric>().spawnEntity(this, entity, func)
}