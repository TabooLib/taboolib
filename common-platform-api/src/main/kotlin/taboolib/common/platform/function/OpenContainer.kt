package taboolib.common.platform.function

import taboolib.common.OpenContainer
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformOpenContainer

/**
 * 获取当前服务端中运行的所有开放接口
 */
fun getOpenContainers(): List<OpenContainer> {
    return PlatformFactory.getService<PlatformOpenContainer>().getOpenContainers()
}

/**
 * 获取特定名称的开放接口
 *
 * @param name 接口名称
 */
fun getOpenContainer(name: String): OpenContainer? {
    return PlatformFactory.getService<PlatformOpenContainer>().getOpenContainers().firstOrNull { it.name == name }
}