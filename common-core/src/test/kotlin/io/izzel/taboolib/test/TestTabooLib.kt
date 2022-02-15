package io.izzel.taboolib.test

import org.junit.jupiter.api.*
import taboolib.common.LifeCycle
import taboolib.common.TabooLibApplication
import taboolib.common.TabooLib
import taboolib.common.env.RuntimeEnv
import taboolib.common.env.SimpleRuntimeEnv
import taboolib.common.inject.InjectHandler
import taboolib.common.io.*
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.internal.SimpleBooster
import taboolib.internal.SimpleInjectHandler
import taboolib.internal.SimpleMonitor
import taboolib.internal.SimplePlatformFactory

/**
 * @author 坏黑
 * @since 2022/1/28 5:00 PM
 */
@Suppress("MaxLineLength")
class TestTabooLib {

    class TestObjectAwaken

    object TestObject {

        var enabled = false

        @Awake(LifeCycle.ENABLE)
        fun enable() {
            enabled = true
        }
    }

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
        assert(InjectHandler.INSTANCE is SimpleInjectHandler)
        assert(PlatformFactory.INSTANCE is SimplePlatformFactory)
        assert(ClassReader.INSTANCE is TestClassReader)
    }

    @Test
    fun testInstance() {
        assert(TestObject::class.java.findInstance().get() == TestObject)
        assert(TestObjectAwaken::class.java.findInstance().get() == PlatformFactory.getAwakeInstance<TestObjectAwaken>())
    }

    @Test
    fun testAwakeFunction() {
        assert(TestObject.enabled)
    }

    @Test
    fun testSignature() {
        assert(groupId == null)
        assert(taboolibId == "taboolib")
        assert(taboolibPath == null)
        assert(TestTabooLib::class.java.groupId == "io.izzel")
    }
}
