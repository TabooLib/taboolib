package taboolib.expansion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral

/**
 * CommandHelper 输出格式单元测试
 *
 * 测试 CommandHelper.buildHelperText / buildDescriptionHelperText 的字符串输出格式，
 * 不依赖 Minecraft 平台，直接构造 CommandComponent 树进行验证。
 */
class CommandHelperTest {

    // ===== 工具方法 =====

    private fun literal(
        vararg aliases: String,
        hidden: Boolean = false,
        description: String = "",
        index: Int = 0
    ): CommandComponentLiteral {
        return CommandComponentLiteral(arrayOf(*aliases), hidden, description, index, false, "")
    }

    private fun dynamic(
        comment: String,
        optional: Boolean = false,
        description: String = "",
        index: Int = 0
    ): CommandComponentDynamic {
        return CommandComponentDynamic(comment, description, index, optional, "")
    }

    // ===== CommandHelper.buildHelperText 测试 =====

    @Test
    fun singleLiteralChildInlined() {
        // 单个 literal 子节点时应内联在同一行显示，格式：/test add
        val add = literal("add", index = 0)
        val result = CommandHelper.buildHelperText("test", listOf(add))
        assertEquals("§cUsage: /test §cadd", result)
    }

    @Test
    fun multipleLiteralChildrenFormatAsTree() {
        // 多个 literal 子节点时应展开为树形，格式：
        //   §cUsage: /test
        //           §7├── §cadd
        //           §7└── §cremove
        val add = literal("add", index = 0)
        val remove = literal("remove", index = 0)
        val result = CommandHelper.buildHelperText("test", listOf(add, remove))
        val lines = result.lines()
        assertEquals("§cUsage: /test", lines[0])
        assertEquals("        §7├── §cadd", lines[1])
        assertEquals("        §7└── §cremove", lines[2])
    }

    @Test
    fun singleDynamicChildShowsRequiredParam() {
        // 单个必填 dynamic 节点应内联显示尖括号格式：/test <player>
        val player = dynamic("player", index = 0)
        val result = CommandHelper.buildHelperText("test", listOf(player))
        assertEquals("§cUsage: /test §7<player>", result)
    }

    @Test
    fun optionalDynamicChildShowsBracketParam() {
        // optional dynamic 节点应显示方括号格式：/test [<player>]
        val player = dynamic("player", optional = true, index = 0)
        val result = CommandHelper.buildHelperText("test", listOf(player))
        assertEquals("§cUsage: /test §8[<player>]", result)
    }

    @Test
    fun literalFollowedByDynamicChild() {
        // literal 后跟 dynamic 子节点时应内联拼接：/test add <player>
        val add = literal("add", index = 0)
        val player = dynamic("player", index = 1)
        add.children.add(player)
        player.parent = add
        val result = CommandHelper.buildHelperText("test", listOf(add))
        assertEquals("§cUsage: /test §cadd §7<player>", result)
    }

    @Test
    fun multipleLiteralsEachWithDynamicChild() {
        // 多个 literal 各自带 dynamic 子节点时，每行应包含各自的参数
        // 预期格式：
        //   §cUsage: /test
        //           §7├── §cadd §7<player>
        //           §7└── §cremove §7<player>
        val add = literal("add", index = 0)
        val addPlayer = dynamic("player", index = 1)
        add.children.add(addPlayer)
        addPlayer.parent = add

        val remove = literal("remove", index = 0)
        val removePlayer = dynamic("player", index = 1)
        remove.children.add(removePlayer)
        removePlayer.parent = remove

        val result = CommandHelper.buildHelperText("test", listOf(add, remove))
        val lines = result.lines()
        assertEquals("§cUsage: /test", lines[0])
        assert(lines[1].contains("§7├── ") && lines[1].contains("§cadd") && lines[1].contains("§7<player>")) {
            "第二行应包含 ├── add <player>，实际：${lines[1]}"
        }
        assert(lines[2].contains("§7└── ") && lines[2].contains("§cremove") && lines[2].contains("§7<player>")) {
            "第三行应包含 └── remove <player>，实际：${lines[2]}"
        }
    }

    @Test
    fun hiddenLiteralChildIsFiltered() {
        // hidden=true 的 literal 子节点不应出现在输出中，visible 节点应正常显示
        val visible = literal("visible", index = 0)
        val hidden = literal("hidden", hidden = true, index = 0)
        // 将 hidden 挂为 parent 的子节点来触发内部过滤逻辑
        val parent = literal("parent", index = 0)
        parent.children.add(visible)
        parent.children.add(hidden)
        visible.parent = parent
        hidden.parent = parent
        val result = CommandHelper.buildHelperText("test", listOf(parent))
        assert(!result.contains("§chidden")) { "hidden 节点不应出现在输出中，实际：$result" }
        assert(result.contains("§cvisible")) { "visible 节点应出现在输出中，实际：$result" }
    }

    @Test
    fun dynamicCommentWithAtPrefixIsResolvedByLangResolver() {
        // dynamic comment 以 @ 开头时，应通过 langResolver 将 key 转换为实际文本
        val node = dynamic("@my.key", index = 0)
        val result = CommandHelper.buildHelperText("test", listOf(node)) { key -> "已解析:$key" }
        assertEquals("§cUsage: /test §7<已解析:my.key>", result)
    }

    @Test
    fun treeIndentationIsAlways8Spaces() {
        // 树形模式下每个分支行前固定缩进 8 个空格，与命令名长度无关
        val a = literal("a", index = 0)
        val b = literal("b", index = 0)
        val result = CommandHelper.buildHelperText("longcmd", listOf(a, b))
        val lines = result.lines()
        assert(lines[1].startsWith("        §7├── ")) { "期望 8 空格缩进，实际：'${lines[1]}'" }
        assert(lines[2].startsWith("        §7└── ")) { "期望 8 空格缩进，实际：'${lines[2]}'" }
    }

    @Test
    fun lastLiteralUsesCornerBranchSymbol() {
        // 非末尾节点使用 ├──，末尾节点使用 └──
        val a = literal("a", index = 0)
        val b = literal("b", index = 0)
        val c = literal("c", index = 0)
        val result = CommandHelper.buildHelperText("test", listOf(a, b, c))
        val lines = result.lines()
        assert(lines[1].contains("§7├── §ca")) { "a 应使用 ├──，实际：${lines[1]}" }
        assert(lines[2].contains("§7├── §cb")) { "b 应使用 ├──，实际：${lines[2]}" }
        assert(lines[3].contains("§7└── §cc")) { "c 应使用 └──，实际：${lines[3]}" }
    }

    // ===== buildDescriptionHelperText 测试 =====

    @Test
    fun leafLiteralWithDescriptionAppendsDescAtEnd() {
        // leaf literal 节点有描述时，应在行尾追加 " §7- §c描述" 格式
        val add = literal("add", description = "添加玩家", index = 0)
        val result = CommandHelper.buildDescriptionHelperText("test", listOf(add))
        assert(result.contains("§7- §c添加玩家")) { "应包含描述，实际：$result" }
    }

    @Test
    fun literalDescriptionPropagatesDownToDynamicLeaf() {
        // literal 节点的描述应向下传递给无描述的 dynamic leaf 子节点
        // 预期：/test add <player> §7- §c添加对象
        val add = literal("add", description = "添加对象", index = 0)
        val player = dynamic("player", index = 1)
        add.children.add(player)
        player.parent = add
        val result = CommandHelper.buildDescriptionHelperText("test", listOf(add))
        assert(result.contains("§7- §c添加对象")) { "literal 描述应传递到叶子节点，实际：$result" }
    }

    @Test
    fun leafDynamicWithDescriptionAppendsDescAtEnd() {
        // leaf dynamic 节点有描述时，应在行尾追加描述
        val player = dynamic("player", description = "目标玩家", index = 0)
        val result = CommandHelper.buildDescriptionHelperText("test", listOf(player))
        assert(result.contains("§7- §c目标玩家")) { "应包含 dynamic 描述，实际：$result" }
    }

    @Test
    fun multipleLeavesEachShowOwnDescription() {
        // 多个 leaf literal 各自有描述时，每个节点末尾应显示各自的描述
        val add = literal("add", description = "添加", index = 0)
        val remove = literal("remove", description = "删除", index = 0)
        val result = CommandHelper.buildDescriptionHelperText("test", listOf(add, remove))
        assert(result.contains("§7- §c添加")) { "add 描述应存在，实际：$result" }
        assert(result.contains("§7- §c删除")) { "remove 描述应存在，实际：$result" }
    }

    @Test
    fun descriptionWithAtPrefixIsResolvedByLangResolver() {
        // 描述字段以 @ 开头时，应通过 langResolver 解析为实际文本
        val add = literal("add", description = "@cmd.add.desc", index = 0)
        val result = CommandHelper.buildDescriptionHelperText("test", listOf(add)) { key -> "描述:$key" }
        assert(result.contains("§7- §c描述:cmd.add.desc")) { "描述应被解析，实际：$result" }
    }

    @Test
    fun leafWithNoDescriptionDoesNotAppendSeparator() {
        // 无描述的 leaf 节点不应追加 "§7- §c" 分隔符
        val add = literal("add", index = 0)
        val result = CommandHelper.buildDescriptionHelperText("test", listOf(add))
        assert(!result.contains("§7- §c")) { "无描述时不应追加分隔符，实际：$result" }
    }

    @Test
    fun dynamicCommentWithAtPrefixResolvedInDescriptionHelper() {
        // buildDescriptionHelperText 中 dynamic comment 的 @ 前缀同样通过 langResolver 解析
        val node = dynamic("@target", index = 0)
        val result = CommandHelper.buildDescriptionHelperText("test", listOf(node)) { key -> "目标:$key" }
        assertEquals("§cUsage: /test §7<目标:target>", result)
    }

    @Test
    fun complexCommandTreeFormatsCorrectly() {
        // 复杂命令树：模拟一个带多层子命令的插件管理命令
        // 预期结构：
        //   /plugin
        //           §7├── §creload
        //           §7├── §cenable §7<name>
        //           §7├── §cdisable §7<name>
        //           §7└── §cinfo §7<name> §8[<page>]
        val reload = literal("reload", description = "重载插件配置", index = 0)

        val enable = literal("enable", description = "启用插件", index = 0)
        val enableName = dynamic("name", index = 1)
        enable.children.add(enableName)
        enableName.parent = enable

        val disable = literal("disable", description = "禁用插件", index = 0)
        val disableName = dynamic("name", index = 1)
        disable.children.add(disableName)
        disableName.parent = disable

        val info = literal("info", description = "查看插件信息", index = 0)
        val infoName = dynamic("name", index = 1)
        val infoPage = dynamic("page", optional = true, index = 2)
        info.children.add(infoName)
        infoName.parent = info
        infoName.children.add(infoPage)
        infoPage.parent = infoName

        val rootChildren = listOf(reload, enable, disable, info)
        val result = CommandHelper.buildDescriptionHelperText("plugin", rootChildren)
        val lines = result.lines()

        // 第一行：命令头
        assertEquals("§cUsage: /plugin", lines[0])

        // reload 行：leaf literal，末尾追加描述
        assert(lines[1].contains("§7├── ") && lines[1].contains("§creload") && lines[1].contains("§7- §c重载插件配置")) {
            "reload 行格式不符，实际：${lines[1]}"
        }

        // enable 行：literal + dynamic <name>，末尾追加描述
        assert(
            lines[2].contains("§7├── ") && lines[2].contains("§cenable") && lines[2].contains("§7<name>") && lines[2].contains(
                "§7- §c启用插件"
            )
        ) {
            "enable 行格式不符，实际：${lines[2]}"
        }

        // disable 行：literal + dynamic <name>，末尾追加描述
        assert(
            lines[3].contains("§7├── ") && lines[3].contains("§cdisable") && lines[3].contains("§7<name>") && lines[3].contains(
                "§7- §c禁用插件"
            )
        ) {
            "disable 行格式不符，实际：${lines[3]}"
        }

        // info 行：literal + dynamic <name> + optional [<page>]，末尾追加描述
        assert(
            lines[4].contains("§7└── ") && lines[4].contains("§cinfo") && lines[4].contains("§7<name>") && lines[4].contains(
                "§8[<page>]"
            ) && lines[4].contains("§7- §c查看插件信息")
        ) {
            "info 行格式不符，实际：${lines[4]}"
        }
    }
}
