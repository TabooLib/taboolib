package taboolib.common.inject

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertThrows
import org.tabooproject.reflex.ReflexClass
import taboolib.common.Requires

/**
 * @Requires 注解条件检查的单元测试
 */
class RequiresTest {

    private val testProperties = mutableListOf<String>()

    @BeforeEach
    fun setUp() {
        testProperties.clear()
    }

    @AfterEach
    fun tearDown() {
        // 清理测试期间设置的系统属性
        testProperties.forEach { System.clearProperty(it) }
    }

    private fun setSystemProperty(key: String, value: String) {
        System.setProperty(key, value)
        testProperties.add(key)
    }

    private fun reflexOf(clazz: Class<*>): ReflexClass {
        return ReflexClass.of(clazz, true)
    }

    // ==================== 实际注解测试类 ====================

    // 无注解
    class NoAnnotation

    // 要求存在的类（存在）
    @Requires(classes = ["java.lang.String"])
    class RequiresExistingClass

    // 要求存在的类（不存在）
    @Requires(classes = ["com.example.NonExistent"])
    class RequiresNonExistentClass

    // 要求多个类都存在（AND）
    @Requires(classes = ["java.lang.String", "java.util.List"])
    class RequiresMultipleClasses

    // 要求类不存在
    @Requires(missingClasses = ["com.example.NonExistent"])
    class RequiresMissingClass

    // 要求类不存在（但实际存在）
    @Requires(missingClasses = ["java.lang.String"])
    class RequiresMissingButExists

    // 系统属性条件
    @Requires(systemProperty = ["server.type=main"])
    class RequiresMainServer

    @Requires(systemProperty = ["server.type=lobby"])
    class RequiresLobbyServer

    // 系统属性不等于条件
    @Requires(systemProperty = ["server.type!=lobby"])
    class RequiresNotLobbyServer

    // 仅检查系统属性存在
    @Requires(systemProperty = ["debug.mode"])
    class RequiresDebugMode

    // 环境变量条件
    @Requires(env = ["PATH"])
    class RequiresPathEnv

    @Requires(env = ["NONEXISTENT_VAR_12345"])
    class RequiresNonExistentEnv

    // 环境变量不等于条件（PATH 存在且不等于 nonexistent）
    @Requires(env = ["PATH!=nonexistent"])
    class RequiresPathNotEquals

    // 组合条件（AND）
    @Requires(classes = ["java.lang.String"], systemProperty = ["test.enabled=true"])
    class RequiresCombinedAnd

    // ==================== checkRequires 实际注解测试 ====================

    @Test
    @DisplayName("无注解的类应该通过")
    fun testNoAnnotation() {
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(NoAnnotation::class.java)))
    }

    @Test
    @DisplayName("@Requires(classes): 类存在时通过")
    fun testRequiresClass_exists() {
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresExistingClass::class.java)))
    }

    @Test
    @DisplayName("@Requires(classes): 类不存在时不通过")
    fun testRequiresClass_notExists() {
        assertFalse(ClassVisitorHandler.checkRequires(reflexOf(RequiresNonExistentClass::class.java)))
    }

    @Test
    @DisplayName("@Requires(classes): 多个类都存在时通过")
    fun testRequiresMultipleClasses_allExist() {
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresMultipleClasses::class.java)))
    }

    @Test
    @DisplayName("@Requires(missingClasses): 类不存在时通过")
    fun testRequiresMissingClass_notExists() {
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresMissingClass::class.java)))
    }

    @Test
    @DisplayName("@Requires(missingClasses): 类存在时不通过")
    fun testRequiresMissingClass_exists() {
        assertFalse(ClassVisitorHandler.checkRequires(reflexOf(RequiresMissingButExists::class.java)))
    }

    @Test
    @DisplayName("@Requires(systemProperty): 属性匹配时通过")
    fun testRequiresSystemProperty_match() {
        setSystemProperty("server.type", "main")
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresMainServer::class.java)))
    }

    @Test
    @DisplayName("@Requires(systemProperty): 属性不匹配时不通过")
    fun testRequiresSystemProperty_mismatch() {
        setSystemProperty("server.type", "lobby")
        assertFalse(ClassVisitorHandler.checkRequires(reflexOf(RequiresMainServer::class.java)))
    }

    @Test
    @DisplayName("@Requires(systemProperty): 属性不存在时不通过")
    fun testRequiresSystemProperty_notExists() {
        assertFalse(ClassVisitorHandler.checkRequires(reflexOf(RequiresMainServer::class.java)))
    }

    @Test
    @DisplayName("@Requires(systemProperty): 不等于 - 值不同时通过")
    fun testRequiresSystemProperty_notEquals_different() {
        setSystemProperty("server.type", "main")
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresNotLobbyServer::class.java)))
    }

    @Test
    @DisplayName("@Requires(systemProperty): 不等于 - 值相同时不通过")
    fun testRequiresSystemProperty_notEquals_same() {
        setSystemProperty("server.type", "lobby")
        assertFalse(ClassVisitorHandler.checkRequires(reflexOf(RequiresNotLobbyServer::class.java)))
    }

    @Test
    @DisplayName("@Requires(systemProperty): 不等于 - 属性不存在时通过")
    fun testRequiresSystemProperty_notEquals_notExists() {
        // server.type 不存在，视为不等于 lobby
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresNotLobbyServer::class.java)))
    }

    @Test
    @DisplayName("@Requires(systemProperty): 仅检查存在 - 存在时通过")
    fun testRequiresSystemProperty_existsOnly_present() {
        setSystemProperty("debug.mode", "any")
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresDebugMode::class.java)))
    }

    @Test
    @DisplayName("@Requires(systemProperty): 仅检查存在 - 不存在时不通过")
    fun testRequiresSystemProperty_existsOnly_absent() {
        assertFalse(ClassVisitorHandler.checkRequires(reflexOf(RequiresDebugMode::class.java)))
    }

    @Test
    @DisplayName("@Requires(env): 环境变量存在时通过")
    fun testRequiresEnv_exists() {
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresPathEnv::class.java)))
    }

    @Test
    @DisplayName("@Requires(env): 环境变量不存在时不通过")
    fun testRequiresEnv_notExists() {
        assertFalse(ClassVisitorHandler.checkRequires(reflexOf(RequiresNonExistentEnv::class.java)))
    }

    @Test
    @DisplayName("@Requires(env): 不等于 - PATH 存在且值不同时通过")
    fun testRequiresEnv_notEquals_different() {
        // PATH 环境变量存在且不等于 "nonexistent"
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresPathNotEquals::class.java)))
    }

    @Test
    @DisplayName("组合条件(AND): 全部满足时通过")
    fun testCombinedAnd_allMatch() {
        setSystemProperty("test.enabled", "true")
        assertTrue(ClassVisitorHandler.checkRequires(reflexOf(RequiresCombinedAnd::class.java)))
    }

    @Test
    @DisplayName("组合条件(AND): 部分满足时不通过")
    fun testCombinedAnd_partialMatch() {
        // class 存在，但 systemProperty 不满足
        assertFalse(ClassVisitorHandler.checkRequires(reflexOf(RequiresCombinedAnd::class.java)))
    }

    // ==================== 辅助方法单元测试 ====================

    @Test
    @DisplayName("isClassPresent: 存在的类返回 true")
    fun testClassPresent_exists() {
        assertTrue(ClassVisitorHandler.isClassPresent("java.lang.String"))
        assertTrue(ClassVisitorHandler.isClassPresent("java.util.List"))
    }

    @Test
    @DisplayName("isClassPresent: 不存在的类返回 false")
    fun testClassPresent_notExists() {
        assertFalse(ClassVisitorHandler.isClassPresent("com.example.NonExistentClass"))
    }

    @Test
    @DisplayName("checkSystemProperty: 键值匹配返回 true")
    fun testSystemProperty_keyValueMatch() {
        setSystemProperty("server.type", "main")
        assertTrue(ClassVisitorHandler.checkSystemProperty("server.type=main"))
    }

    @Test
    @DisplayName("checkSystemProperty: 键值不匹配返回 false")
    fun testSystemProperty_keyValueMismatch() {
        setSystemProperty("server.type", "lobby")
        assertFalse(ClassVisitorHandler.checkSystemProperty("server.type=main"))
    }

    @Test
    @DisplayName("checkSystemProperty: 值中包含等号")
    fun testSystemProperty_valueContainsEquals() {
        setSystemProperty("complex.key", "a=b=c")
        assertTrue(ClassVisitorHandler.checkSystemProperty("complex.key=a=b=c"))
    }

    @Test
    @DisplayName("checkSystemProperty: 不等于 - 值不同返回 true")
    fun testSystemProperty_notEquals_different() {
        setSystemProperty("server.type", "main")
        assertTrue(ClassVisitorHandler.checkSystemProperty("server.type!=lobby"))
    }

    @Test
    @DisplayName("checkSystemProperty: 不等于 - 值相同返回 false")
    fun testSystemProperty_notEquals_same() {
        setSystemProperty("server.type", "lobby")
        assertFalse(ClassVisitorHandler.checkSystemProperty("server.type!=lobby"))
    }

    @Test
    @DisplayName("checkSystemProperty: 不等于 - 属性不存在返回 true")
    fun testSystemProperty_notEquals_notExists() {
        assertTrue(ClassVisitorHandler.checkSystemProperty("nonexistent.key!=anyvalue"))
    }

    @Test
    @DisplayName("checkSystemProperty: 不等于 - 值中包含 !=")
    fun testSystemProperty_notEquals_valueContainsOperator() {
        setSystemProperty("complex.key", "a!=b")
        assertTrue(ClassVisitorHandler.checkSystemProperty("complex.key!=other"))
        assertFalse(ClassVisitorHandler.checkSystemProperty("complex.key!=a!=b"))
    }

    @Test
    @DisplayName("checkEnvironmentVariable: PATH 环境变量存在")
    fun testEnvVariable_pathExists() {
        assertTrue(ClassVisitorHandler.checkEnvironmentVariable("PATH"))
    }

    @Test
    @DisplayName("checkEnvironmentVariable: 不存在的环境变量返回 false")
    fun testEnvVariable_notExists() {
        assertFalse(ClassVisitorHandler.checkEnvironmentVariable("TABOOLIB_TEST_NONEXISTENT_VAR"))
    }

    @Test
    @DisplayName("checkEnvironmentVariable: 不等于 - 值不同返回 true")
    fun testEnvVariable_notEquals_different() {
        // PATH 存在且不等于 "nonexistent"
        assertTrue(ClassVisitorHandler.checkEnvironmentVariable("PATH!=nonexistent"))
    }

    @Test
    @DisplayName("checkEnvironmentVariable: 不等于 - 变量不存在返回 true")
    fun testEnvVariable_notEquals_notExists() {
        assertTrue(ClassVisitorHandler.checkEnvironmentVariable("TABOOLIB_TEST_NONEXISTENT_VAR!=anyvalue"))
    }

    @Test
    @DisplayName("边界情况: 系统属性键为空时抛出异常 (=)")
    fun testEdgeCase_emptyKey() {
        assertThrows<IllegalArgumentException> {
            ClassVisitorHandler.checkSystemProperty("=value")
        }
    }

    @Test
    @DisplayName("边界情况: 系统属性键为空时抛出异常 (!=)")
    fun testEdgeCase_emptyKey_notEquals() {
        assertThrows<IllegalArgumentException> {
            ClassVisitorHandler.checkSystemProperty("!=value")
        }
    }

    @Test
    @DisplayName("边界情况: 环境变量键为空时抛出异常 (!=)")
    fun testEdgeCase_emptyEnvKey_notEquals() {
        assertThrows<IllegalArgumentException> {
            ClassVisitorHandler.checkEnvironmentVariable("!=value")
        }
    }

    // ==================== 场景测试 ====================

    @Test
    @DisplayName("场景: 主服与登录服互斥")
    fun testScenario_serverTypeExclusive() {
        setSystemProperty("server.type", "main")

        val mainServer = reflexOf(RequiresMainServer::class.java)
        val lobbyServer = reflexOf(RequiresLobbyServer::class.java)

        assertTrue(ClassVisitorHandler.checkRequires(mainServer))
        assertFalse(ClassVisitorHandler.checkRequires(lobbyServer))

        // 切换到登录服
        setSystemProperty("server.type", "lobby")

        assertFalse(ClassVisitorHandler.checkRequires(mainServer))
        assertTrue(ClassVisitorHandler.checkRequires(lobbyServer))
    }
}
