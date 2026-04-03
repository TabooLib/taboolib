import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.reader.UnicodeReader
import taboolib.library.configuration.BukkitYaml
import taboolib.library.configuration.YamlConstructor
import taboolib.library.configuration.YamlRepresenter
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

/**
 * 直接调用 SnakeYAML compose 验证 ClassCastException
 *
 * @author sky
 */
class TestYamlComposeDirect {

    /**
     * 此 YAML 结构复现了生产环境的 ClassCastException：
     * 多行块标量 + 被注释掉的整段 YAML 结构体
     */
    private val problemYaml = buildString {
        appendLine("# 世界设置")
        appendLine("world:")
        appendLine("  # 世界时间")
        appendLine("  time: 6000")
        appendLine("  # 昼夜循环")
        appendLine("  daylight-cycle: false")
        appendLine("  # 队友伤害")
        appendLine("  pvp: false")
        appendLine()
        appendLine("# 阶段配置")
        appendLine("stages:")
        appendLine("  stage1:")
        appendLine("    goals:")
        appendLine("      goal1:")
        appendLine("        # 目标名称")
        appendLine("        name: first-goal")
        appendLine("        # 目标描述")
        appendLine("        description: test")
        appendLine("        # 信号类型")
        appendLine("        signal: START_SIGNAL")
        appendLine("        # 信号需要触发的次数")
        appendLine("        count: 1")
        appendLine("        # 该目标是否可选")
        appendLine("        optional: false")
        appendLine("        prevent-remove: true")
        appendLine("  stage2:")
        appendLine("    goals:")
        appendLine("      goal1:")
        appendLine("        name: kill-monster")
        appendLine("        signal: ON_MONSTER_DEATH")
        appendLine("        criteria:")
        appendLine("         mobId: == test.monster")
        appendLine("        count: 1")
        appendLine("        optional: false")
        appendLine("        prevent-remove: true")
        appendLine("        then: |-")
        appendLine("          debug \"stage2 completed\"")
        appendLine("            for p in players then {")
        appendLine("            switch &p")
        appendLine("            action-lock disable")
        appendLine("            select abc-123 set-stage stage3")
        appendLine("          }")
        appendLine("  stage3:")
        appendLine("    goals:")
        appendLine("      goal1:")
        appendLine("        # 目标名称")
        appendLine("        name: talk-to-npc")
        appendLine("        # 目标描述")
        appendLine("        description: test")
        appendLine("        # 信号类型")
        appendLine("        signal: FOLLOW_SIGNAL")
        appendLine("        # 信号需要触发的次数")
        appendLine("        count: 1")
        appendLine("        # 该目标是否可选")
        appendLine("        optional: false")
        appendLine("        prevent-remove: true")
        appendLine("        then: |-")
        appendLine("          for p in players then {")
        appendLine("            switch &p")
        appendLine("            select npc meta set pose to STANDING")
        appendLine("            select npc chat play \"message\"")
        appendLine("            select npc follow enable speed 0.2 -await")
        appendLine("            action-lock enable")
        appendLine("          }")
        appendLine()
        appendLine("  # stage4:")
        appendLine("  #   goals:")
        appendLine("  #     goal1:")
        appendLine("  #       # 目标名称")
        appendLine("  #       name: explore-more")
        appendLine("  #       # 目标描述")
        appendLine("  #       description: test")
        appendLine("  #       # 信号类型")
        appendLine("  #       signal: JOIN_BATTLE")
        appendLine("  #       # 信号需要触发的次数")
        appendLine("  #       count: 1")
        appendLine("  #       # 该目标是否可选")
        appendLine("  #       optional: false")
        appendLine("  #       prevent-remove: true")
        appendLine("  #       then: |-")
        appendLine("  #         signal MAKE_SPAWNER array [ boss test.boss_mob ]")
        appendLine("  #         for p in players then {")
        appendLine("  #         switch &p")
        appendLine("  #         select npc chat play \"message\"")
        appendLine("  #         select npc follow disable")
        appendLine("  #         equipment mainhand item:weapon@hand")
        appendLine("  #         select npc play-await @/uuid/animation")
        appendLine("  #         }")
        appendLine()
        appendLine("  # stage5:")
        appendLine("  #   goals:")
        appendLine("  #     goal1:")
        appendLine("  #       name: defeat-final-boss")
        appendLine("  #       signal: ON_MONSTER_DEATH")
        appendLine("  #       criteria:")
        appendLine("  #        mobId: == test.final_boss")
        appendLine("  #       count: 1")
        appendLine("  #       optional: false")
        appendLine("  #       prevent-remove: true")
        appendLine("  #       then: |-")
        appendLine("  #         debug \"all stages completed\"")
        appendLine("  #         select npc chat play \"message\"")
        appendLine()
        appendLine()
        appendLine("events:")
        appendLine("  on_start:")
        appendLine("    signal: START_SIGNAL")
        appendLine("    then: |-")
        appendLine("      signal MAKE_SPAWNER array [ group1 test.monster ]")
        appendLine("      for p in players then {")
        appendLine("        switch &p")
        appendLine("        select npc chat play \"message\"")
        appendLine("        select npc meta set pose to CROUCHING")
        appendLine("        action-lock enable")
        appendLine("      }")
    }

    /**
     * 使用原生 Yaml.compose() 解析含注释的复杂 YAML 会崩溃
     */
    @Test
    fun testVanillaSnakeYamlComposeFails() {
        val loaderOptions = LoaderOptions()
        loaderOptions.maxAliasesForCollections = Integer.MAX_VALUE
        loaderOptions.isProcessComments = true
        val dumperOptions = DumperOptions()
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val representer = YamlRepresenter(dumperOptions)
        val constructor = YamlConstructor(loaderOptions)
        val yaml = Yaml(constructor, representer, dumperOptions, loaderOptions)
        assertThrows(ClassCastException::class.java) {
            UnicodeReader(ByteArrayInputStream(problemYaml.toByteArray(StandardCharsets.UTF_8))).use { reader ->
                yaml.compose(reader)
            }
        }
    }

    /**
     * 使用 BukkitYaml.compose() 解析同样的 YAML 应该成功
     */
    @Test
    fun testBukkitYamlComposeSucceeds() {
        val loaderOptions = LoaderOptions()
        loaderOptions.maxAliasesForCollections = Integer.MAX_VALUE
        loaderOptions.isProcessComments = true
        val dumperOptions = DumperOptions()
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val representer = YamlRepresenter(dumperOptions)
        val constructor = YamlConstructor(loaderOptions)
        val yaml = BukkitYaml(constructor, representer, dumperOptions, loaderOptions)
        assertDoesNotThrow {
            UnicodeReader(ByteArrayInputStream(problemYaml.toByteArray(StandardCharsets.UTF_8))).use { reader ->
                yaml.compose(reader)
            }
        }
    }
}
