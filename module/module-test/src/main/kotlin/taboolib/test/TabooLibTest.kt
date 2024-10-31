package taboolib.test

import taboolib.common.LifeCycle
import taboolib.common.Test
import taboolib.common.platform.Awake
import taboolib.common.platform.command.simpleCommand

/**
 * TabooLib
 * taboolib.test.TabooLibTest
 *
 * @author 坏黑
 * @since 2024/6/19 01:00
 */
object TabooLibTest {

    @Awake(LifeCycle.ENABLE)
    fun init() {
        simpleCommand("taboolibTest") { _, args ->
            run(args.firstOrNull() == "detail")
        }
    }

    fun run(detail: Boolean = true) {
        Test.check(
            TestBukkitAttribute,
            TestDataSerializer,
            TestItemTag,
            TestLocaleI18n,
            TestMinecraftServerUtil,
            TestNMSEntity,
            TestNMSI18n,
            TestNMSMessage,
            TestNMSParticle,
            TestNMSScoreboard,
            TestNMSToast,
            TestPacketSender,
            TestSimpleAi
        ).print(detail)
    }
}