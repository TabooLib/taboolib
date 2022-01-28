package taboolib.test

import org.junit.jupiter.api.*
import taboolib.common.io.runningClasses
import taboolib.common.TabooLibApplication
import taboolib.common.TabooLib
import taboolib.common.env.RuntimeEnv
import taboolib.common.env.SimpleRuntimeEnv
import taboolib.common.inject.InjectorHandler
import taboolib.common.io.ClassReader
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.internal.SimpleBooster
import taboolib.internal.SimpleInjectorHandler
import taboolib.internal.SimpleMonitor
import taboolib.internal.SimplePlatformFactory

/**
 * TabooLib
 * PACKAGE_NAME.TestTabooLib
 *
 * @author 坏黑
 * @since 2022/1/28 5:00 PM
 */
class TestTabooLib {

    @BeforeEach
    internal fun setUp() {
        TabooLibApplication.startsNow()
        assert(!TabooLib.monitor().isShutdown)
        assert(TabooLib.runningPlatform() == Platform.APPLICATION)
    }

    @AfterEach
    internal fun tearDown() {
        TabooLibApplication.disableNow()
    }

    @Test
    fun testClassReader() {
        assert(runningClasses.isNotEmpty())
        runningClasses.first { it == TabooLib::class.java }
        runningClasses.first { it == TestTabooLib::class.java }
    }

    @Test
    fun testService() {
        assert(TabooLib.booster() is SimpleBooster)
        assert(TabooLib.monitor() is SimpleMonitor)
        assert(RuntimeEnv.INSTANCE is SimpleRuntimeEnv)
        assert(InjectorHandler.INSTANCE is SimpleInjectorHandler)
        assert(PlatformFactory.INSTANCE is SimplePlatformFactory)
        assert(ClassReader.INSTANCE is TestClassReader)
    }
}