package taboolib.module.porticus

import java.util.*

/**
 * Porticus
 * taboolib.module.porticus.api.API
 *
 *
 * Porticus API 抽象类
 *
 * @author bkm016
 * @since 2020/10/15 9:45 下午
 */
abstract class API {

    /**
     * 获取 API 类型
     *
     * @return [APIType]
     */
    abstract val type: APIType

    /**
     * 新建一个通讯任务
     *
     * @return [PorticusMission]
     */
    abstract fun createMission(): PorticusMission

    abstract fun createMission(uid: UUID): PorticusMission
}