package taboolib.common.platform.function

import taboolib.common.OpenContainer
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformOpenContainer

fun getOpenContainers(): List<OpenContainer> {
    return PlatformFactory.getPlatformService<PlatformOpenContainer>().getOpenContainers()
}

fun getOpenContainer(name: String): OpenContainer? {
    return PlatformFactory.getPlatformService<PlatformOpenContainer>().getOpenContainers().firstOrNull { it.name == name }
}