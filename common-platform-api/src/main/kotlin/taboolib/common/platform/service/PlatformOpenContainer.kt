package taboolib.common.platform.service

import taboolib.common.OpenContainer
import taboolib.common.platform.PlatformService

@PlatformService
interface PlatformOpenContainer {

    fun getOpenContainers(): List<OpenContainer>
}