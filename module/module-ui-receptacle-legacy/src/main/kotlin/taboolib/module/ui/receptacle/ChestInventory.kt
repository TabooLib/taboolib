package taboolib.module.ui.receptacle

import kotlin.properties.Delegates

/**
 * @author Arasple
 * @date 2020/11/29 10:59
 */
class ChestInventory(rows: Int = 3, title: String = "Chest") : Receptacle(ReceptacleType.ofRows(rows), title) {

    var rows: Int by Delegates.observable(rows) { _, _, value ->
        type = ReceptacleType.ofRows(value)
    }
}