package taboolib.module.nms

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import java.util.concurrent.CompletableFuture

@Awake(LifeCycle.ENABLE)
fun nmsPreheat(): CompletableFuture<Void> {
    return CompletableFuture.runAsync {
        nmsProxy<NMSEntity>()
        nmsProxy<NMSItem>()
        nmsProxy<NMSItemTag>()
        nmsProxy<NMSLight>()
        nmsProxy<NMSMessage>()
        nmsProxy<NMSParticle>()
        nmsProxy<NMSScoreboard>()
        nmsProxy<NMSSign>()
    }
}