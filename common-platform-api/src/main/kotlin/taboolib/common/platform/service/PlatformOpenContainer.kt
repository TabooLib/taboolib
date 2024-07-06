package taboolib.common.platform.service

import taboolib.common.Inject
import taboolib.common.OpenContainer
import taboolib.common.platform.PlatformService

@Inject
@PlatformService
interface PlatformOpenContainer {

    fun getOpenContainers(): List<OpenContainer>
}