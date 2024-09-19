package taboolib.expansion

import taboolib.module.database.Host
import taboolib.module.database.Table

/**
 * 抽象类 Type，定义了数据库操作的基本结构。
 */
abstract class Type {

    /**
     * 获取数据库主机配置。
     *
     * @return 返回 Host 对象，表示数据库连接的主机配置。
     */
    abstract fun host(): Host<*>

    /**
     * 获取数据库表配置。
     *
     * @return 返回 Table 对象，表示数据库表的配置。
     */
    abstract fun tableVar(): Table<*, *>
}