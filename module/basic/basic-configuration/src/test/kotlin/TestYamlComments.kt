import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import taboolib.module.configuration.Configuration

/**
 * 测试 YAML 注释解析不会触发 ClassCastException
 *
 * SnakeYAML 的 ScannerImpl 在分类注释类型时有一个 Branch B：当一个独占一行的注释
 * 与前一个行内注释处于同一列时，会被误判为 IN_LINE 类型。Composer 的
 * blockCommentsCollector 不处理 IN_LINE 事件，导致 composeNode() 中的
 * (NodeEvent) parser.peekEvent() 强转失败。
 *
 * @author sky
 */
class TestYamlComments {

    @Test
    fun testBasicBlockComment() {
        assertDoesNotThrow {
            Configuration.loadFromString("# 注释\nkey: value")
        }
    }

    /**
     * 触发 ScannerImpl 的 Branch B 误分类：
     * pvp: false 后的行内注释位于特定列，
     * 下一行独占注释 # 线性阶段 恰好处于同一列，被误判为 IN_LINE
     */
    @Test
    fun testInlineCommentColumnAlignmentBug() {
        val yaml = buildString {
            appendLine("world:")
            appendLine("  time: 6000")
            appendLine("  pvp: false # 不允许PVP")
            appendLine("             # 这行注释与上方行内注释同列")
            appendLine("  next: value")
        }
        assertDoesNotThrow { Configuration.loadFromString(yaml) }
    }

    /**
     * 行内注释后紧跟同列的独立注释行（多行连续场景）
     */
    @Test
    fun testMultipleAlignedInlineComments() {
        val yaml = buildString {
            appendLine("a:")
            appendLine("  key: val  # comment1")
            appendLine("            # comment2")
            appendLine("            # comment3")
            appendLine("  other: val2")
        }
        assertDoesNotThrow { Configuration.loadFromString(yaml) }
    }

    @Test
    fun testNestedBlockComments() {
        val yaml = buildString {
            appendLine("# 世界设置")
            appendLine("world:")
            appendLine("  # 世界时间")
            appendLine("  time: 6000")
            appendLine("  daylight-cycle: false")
            appendLine("  pvp: false")
            appendLine()
            appendLine("# 阶段配置")
            appendLine("stages:")
            appendLine("  stage1:")
            appendLine("    goals:")
            appendLine("      goal1:")
            appendLine("        name: first-goal")
            appendLine("        signal: ON_DEATH")
            appendLine("        criteria:")
            appendLine("          id: == test.mob_a")
            appendLine("        count: 5")
            appendLine("        optional: false")
            appendLine("  stage2:")
            appendLine("    goals:")
            appendLine("      goal1:")
            appendLine("        name: second-goal")
            appendLine("        signal: ON_DEATH")
            appendLine("        criteria:")
            appendLine("          id: == test.mob_b")
            appendLine("        optional: false")
        }
        assertDoesNotThrow { Configuration.loadFromString(yaml) }
    }

    @Test
    fun testBlankLinesBetweenSections() {
        val yaml = "key1: value1\n\n# 第二部分\nkey2: value2\n\nkey3: value3"
        assertDoesNotThrow { Configuration.loadFromString(yaml) }
    }

    @Test
    fun testInlineComments() {
        assertDoesNotThrow {
            Configuration.loadFromString("key1: value1  # 行内注释\nkey2: value2")
        }
    }

    @Test
    fun testCommentsInDeeplyNestedStructure() {
        val yaml = buildString {
            appendLine("a:")
            appendLine("  b:")
            appendLine("    c:")
            appendLine("      # 深层注释")
            appendLine("      d: value")
            appendLine("      # 另一个注释")
            appendLine("      e: value2")
        }
        assertDoesNotThrow { Configuration.loadFromString(yaml) }
    }

    /**
     * 测试保存再加载（round-trip）不会引入注释问题
     */
    @Test
    fun testSaveAndReload() {
        val yaml = buildString {
            appendLine("# 文件头注释")
            appendLine("section:")
            appendLine("  # 键注释")
            appendLine("  key: value")
        }
        val config = Configuration.loadFromString(yaml)
        val saved = config.saveToString()
        assertDoesNotThrow { Configuration.loadFromString(saved) }
    }

    /**
     * 复杂配置文件（含大量注释、多行字符串、被注释掉的整段 YAML）。
     * 此结构曾在生产环境触发 ClassCastException。
     */
    @Test
    fun testComplexConfigWithCommentedOutSections() {
        val yaml = """
            # 世界设置
            world:
              # 世界时间
              time: 6000
              # 昼夜循环
              daylight-cycle: false
              # 队友伤害
              pvp: false

            # 线性阶段
            stages:
              stage1:
                goals:
                  goal1:
                    # 目标名称
                    name: 完成第一个目标
                    # 目标描述（可选）
                    description: 测试描述
                    # 信号类型
                    signal: START_SIGNAL
                    # 匹配条件
                    # 信号需要触发的次数 (默认为 1)
                    count: 1
                    # 该目标是否可选 (默认为 false)
                    optional: false
                    prevent-remove: true
              stage2:
                goals:
                  goal1:
                    name: 击败目标怪物
                    signal: ON_MONSTER_DEATH
                    criteria:
                     mobId: == test.monster
                    count: 1
                    optional: false
                    prevent-remove: true
                    then: |-
                      debug "stage2 completed"
                        for p in players then {
                        switch &p
                        action-lock disable
                        select abc-123 set-stage stage3
                      }
              stage3:
                goals:
                  goal1:
                    # 目标名称
                    name: 与NPC对话
                    # 目标描述（可选）
                    description: 测试描述
                    # 信号类型
                    signal: FOLLOW_SIGNAL
                    # 匹配条件
                    # 信号需要触发的次数 (默认为 1)
                    count: 1
                    # 该目标是否可选 (默认为 false)
                    optional: false
                    prevent-remove: true
                    then: |-
                      for p in players then {
                        switch &p
                        select npc meta set pose to STANDING
                        select npc chat play "message"
                        select npc follow enable speed 0.2 -await
                        action-lock enable
                      }

              # stage4:
              #   goals:
              #     goal1:
              #       # 目标名称
              #       name: 进一步探索
              #       # 目标描述（可选）
              #       description: 测试描述
              #       # 信号类型
              #       signal: JOIN_BATTLE
              #       # 匹配条件
              #       # 信号需要触发的次数 (默认为 1)
              #       count: 1
              #       # 该目标是否可选 (默认为 false)
              #       optional: false
              #       prevent-remove: true
              #       then: |-
              #         signal MAKE_SPAWNER array [ boss test.boss_mob ]
              #         for p in players then {
              #         switch &p
              #         select npc chat play "message"
              #         select npc follow disable
              #         equipment mainhand item:weapon@hand
              #         select npc play-await @/uuid/animation
              #         }

              # stage5:
              #   goals:
              #     goal1:
              #       name: 击败最终目标
              #       signal: ON_MONSTER_DEATH
              #       criteria:
              #        mobId: == test.final_boss
              #       count: 1
              #       optional: false
              #       prevent-remove: true
              #       then: |-
              #         debug "all stages completed"
              #         select npc chat play "message"


            events:
              on_start:
                signal: START_SIGNAL
                then: |-
                  signal MAKE_SPAWNER array [ group1 test.monster ]
                  for p in players then {
                    switch &p
                    select npc chat play "message"
                    select npc meta set pose to CROUCHING
                    action-lock enable
                  }
        """.trimIndent()
        assertDoesNotThrow { Configuration.loadFromString(yaml) }
    }
}
