package taboolib.common.platform.function

import taboolib.common.OpenContainer
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.common.util.unsafeLazy

val openContainerService by unsafeLazy { PlatformFactory.getService<PlatformOpenContainer>() }

/**
 * 获取当前服务端中运行的所有开放接口
 */
fun getOpenContainers(): List<OpenContainer> {
    return openContainerService.getOpenContainers()
}

/**
 * 获取特定名称的开放接口
 *
 * @param name 接口名称
 */
fun getOpenContainer(name: String): OpenContainer? {
    return openContainerService.getOpenContainer(name)
}