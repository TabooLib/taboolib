@file: Inject

package taboolib.module.nms

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import java.util.concurrent.CompletableFuture

@Awake(LifeCycle.ENABLE)
fun nmsPreheat(): CompletableFuture<Void> {
    return CompletableFuture.runAsync {
        nmsProxy<NMSEntity>()
        nmsProxy<NMSItem>()
        NMSItemTag.instance
        nmsProxy<NMSLight>()
        nmsProxy<NMSMessage>()
        nmsProxy<NMSParticle>()
        nmsProxy<NMSScoreboard>()
        nmsProxy<NMSSign>()
    }
}