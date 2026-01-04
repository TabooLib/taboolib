package taboolib.common.platform.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * TabooLib
 * taboolib.common.platform.command.CommandLineParserTest
 *
 * CommandLineParser 单元测试
 * 
 * @author sky
 * @since 2025/12/22
 */
class CommandLineParserTest {

    // ========== 基础参数测试 ==========
    
    @Test
    fun `test simple arguments`() {
        val parser = CommandLineParser("arg1 arg2 arg3").parse()
        assertEquals(listOf("arg1", "arg2", "arg3"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test empty input`() {
        val parser = CommandLineParser("").parse()
        assertTrue(parser.args.isEmpty())
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test single argument`() {
        val parser = CommandLineParser("single").parse()
        assertEquals(listOf("single"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    // ========== 连字符参数测试（核心 Bug 修复） ==========

    @Test
    fun `test hyphenated argument`() {
        val parser = CommandLineParser("aa-bb").parse()
        assertEquals(listOf("aa-bb"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test multiple hyphenated arguments`() {
        val parser = CommandLineParser("aa-bb cc-dd ee-ff").parse()
        assertEquals(listOf("aa-bb", "cc-dd", "ee-ff"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test uuid format`() {
        val parser = CommandLineParser("550e8400-e29b-41d4-a716-446655440000").parse()
        assertEquals(listOf("550e8400-e29b-41d4-a716-446655440000"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test player name with hyphen`() {
        val parser = CommandLineParser("player-name").parse()
        assertEquals(listOf("player-name"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test version number`() {
        val parser = CommandLineParser("1.0-SNAPSHOT").parse()
        assertEquals(listOf("1.0-SNAPSHOT"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test multiple hyphens in argument`() {
        val parser = CommandLineParser("a-b-c-d-e").parse()
        assertEquals(listOf("a-b-c-d-e"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    // ========== 负数参数测试 ==========

    @Test
    fun `test negative number as argument`() {
        val parser = CommandLineParser("-123").parse()
        assertEquals(listOf("-123"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test negative decimal as argument`() {
        val parser = CommandLineParser("-3.14").parse()
        assertEquals(listOf("-3.14"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test multiple negative numbers`() {
        val parser = CommandLineParser("-1 -2 -3").parse()
        assertEquals(listOf("-1", "-2", "-3"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test negative number with option`() {
        val parser = CommandLineParser("-opt -123").parse()
        assertEquals(mapOf("opt" to ""), parser.options)
        assertEquals(listOf("-123"), parser.args)
    }

    // ========== 基础选项测试 ==========

    @Test
    fun `test single option`() {
        val parser = CommandLineParser("-opt").parse()
        assertEquals(mapOf("opt" to ""), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test multiple options`() {
        val parser = CommandLineParser("-a -b -c").parse()
        assertEquals(mapOf("a" to "", "b" to "", "c" to ""), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test option with equals assignment`() {
        val parser = CommandLineParser("-opt=value").parse()
        assertEquals(mapOf("opt" to "value"), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test option with colon assignment`() {
        val parser = CommandLineParser("-opt:value").parse()
        assertEquals(mapOf("opt" to "value"), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test options with different assignments`() {
        val parser = CommandLineParser("-a=1 -b:2 -c").parse()
        assertEquals(mapOf("a" to "1", "b" to "2", "c" to ""), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    // ========== 混合选项和参数测试 ==========

    @Test
    fun `test option before argument`() {
        val parser = CommandLineParser("-opt arg").parse()
        assertEquals(mapOf("opt" to ""), parser.options)
        assertEquals(listOf("arg"), parser.args)
    }

    @Test
    fun `test argument before option`() {
        val parser = CommandLineParser("arg -opt").parse()
        assertEquals(listOf("arg"), parser.args)
        assertEquals(mapOf("opt" to ""), parser.options)
    }

    @Test
    fun `test option with hyphenated argument`() {
        val parser = CommandLineParser("-opt aa-bb").parse()
        assertEquals(mapOf("opt" to ""), parser.options)
        assertEquals(listOf("aa-bb"), parser.args)
    }

    @Test
    fun `test hyphenated argument with option`() {
        val parser = CommandLineParser("aa-bb -opt").parse()
        assertEquals(listOf("aa-bb"), parser.args)
        assertEquals(mapOf("opt" to ""), parser.options)
    }

    @Test
    fun `test mixed options and hyphenated arguments`() {
        val parser = CommandLineParser("-a=x-y -b y-z").parse()
        assertEquals(mapOf("a" to "x-y", "b" to ""), parser.options)
        assertEquals(listOf("y-z"), parser.args)
    }

    @Test
    fun `test complex mixing`() {
        val parser = CommandLineParser("-verbose -level=hard -count:5 player-name item-type").parse()
        assertEquals(mapOf("verbose" to "", "level" to "hard", "count" to "5"), parser.options)
        assertEquals(listOf("player-name", "item-type"), parser.args)
    }

    // ========== 引号处理测试 ==========

    @Test
    fun `test single quoted argument`() {
        val parser = CommandLineParser("'hello world'").parse()
        assertEquals(listOf("hello world"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test double quoted argument`() {
        val parser = CommandLineParser("\"hello world\"").parse()
        assertEquals(listOf("hello world"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test option with single quoted value`() {
        val parser = CommandLineParser("-msg='Hello World'").parse()
        assertEquals(mapOf("msg" to "Hello World"), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test option with double quoted value`() {
        val parser = CommandLineParser("-msg=\"Hello World\"").parse()
        assertEquals(mapOf("msg" to "Hello World"), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test mixed quotes`() {
        val parser = CommandLineParser("-a='N M S L' \"1 2 3\"").parse()
        assertEquals(mapOf("a" to "N M S L"), parser.options)
        assertEquals(listOf("1 2 3"), parser.args)
    }

    @Test
    fun `test hyphen inside quotes`() {
        val parser = CommandLineParser("'aa-bb-cc'").parse()
        assertEquals(listOf("aa-bb-cc"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test option marker inside quotes`() {
        val parser = CommandLineParser("'-not-an-option'").parse()
        assertEquals(listOf("-not-an-option"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    // ========== 转义字符测试 ==========

    @Test
    fun `test escaped single quote`() {
        val parser = CommandLineParser("\\'test\\'").parse()
        assertEquals(listOf("'test'"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test escaped double quote`() {
        val parser = CommandLineParser("\\\"test\\\"").parse()
        assertEquals(listOf("\"test\""), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test escaped hyphen`() {
        val parser = CommandLineParser("aa\\-bb").parse()
        assertEquals(listOf("aa-bb"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test escaped equals`() {
        val parser = CommandLineParser("a\\=b").parse()
        assertEquals(listOf("a=b"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test escaped colon`() {
        val parser = CommandLineParser("a\\:b").parse()
        assertEquals(listOf("a:b"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test escaped backslash`() {
        val parser = CommandLineParser("a\\\\b").parse()
        assertEquals(listOf("a\\b"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test option with escaped hyphen in value`() {
        val parser = CommandLineParser("-opt=val\\-ue").parse()
        assertEquals(mapOf("opt" to "val-ue"), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    // ========== 边缘情况测试 ==========

    @Test
    fun `test single hyphen`() {
        val parser = CommandLineParser("-").parse()
        assertEquals(listOf("-"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test double hyphen`() {
        val parser = CommandLineParser("--").parse()
        assertEquals(mapOf("-" to ""), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test triple hyphen`() {
        val parser = CommandLineParser("---").parse()
        assertEquals(mapOf("--" to ""), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test multiple spaces`() {
        val parser = CommandLineParser("arg1    arg2     arg3").parse()
        assertEquals(listOf("arg1", "arg2", "arg3"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test leading spaces`() {
        val parser = CommandLineParser("   arg1 arg2").parse()
        assertEquals(listOf("arg1", "arg2"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test trailing spaces`() {
        val parser = CommandLineParser("arg1 arg2   ").parse()
        assertEquals(listOf("arg1", "arg2"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test empty option name`() {
        val parser = CommandLineParser("- arg").parse()
        assertEquals(listOf("-", "arg"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test option at end`() {
        val parser = CommandLineParser("arg1 arg2 -opt").parse()
        assertEquals(listOf("arg1", "arg2"), parser.args)
        assertEquals(mapOf("opt" to ""), parser.options)
    }

    // ========== 实际使用场景测试 ==========

    @Test
    fun `test give command scenario`() {
        val parser = CommandLineParser("player-name diamond -amount=64 -silent").parse()
        assertEquals(listOf("player-name", "diamond"), parser.args)
        assertEquals(mapOf("amount" to "64", "silent" to ""), parser.options)
    }

    @Test
    fun `test teleport command with negative coordinates`() {
        val parser = CommandLineParser("player-name -100 64 -200").parse()
        assertEquals(listOf("player-name", "-100", "64", "-200"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test complex command with uuid`() {
        val parser = CommandLineParser("-verbose 550e8400-e29b-41d4-a716-446655440000 -level:5").parse()
        assertEquals(mapOf("verbose" to "", "level" to "5"), parser.options)
        assertEquals(listOf("550e8400-e29b-41d4-a716-446655440000"), parser.args)
    }

    @Test
    fun `test maven coordinates`() {
        val parser = CommandLineParser("com.example:artifact:1.0-SNAPSHOT").parse()
        assertEquals(listOf("com.example:artifact:1.0-SNAPSHOT"), parser.args)
        assertTrue(parser.options.isEmpty())
    }

    @Test
    fun `test command with message`() {
        val parser = CommandLineParser("-msg='Player player-name has joined' player-name").parse()
        assertEquals(mapOf("msg" to "Player player-name has joined"), parser.options)
        assertEquals(listOf("player-name"), parser.args)
    }

    // ========== 特殊字符组合测试 ==========

    @Test
    fun `test hyphen after equals`() {
        val parser = CommandLineParser("-opt=-value").parse()
        assertEquals(mapOf("opt" to "-value"), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test hyphen after colon`() {
        val parser = CommandLineParser("-opt:-value").parse()
        assertEquals(mapOf("opt" to "-value"), parser.options)
        assertTrue(parser.args.isEmpty())
    }

    @Test
    fun `test number after option`() {
        val parser = CommandLineParser("-level 5").parse()
        assertEquals(mapOf("level" to ""), parser.options)
        assertEquals(listOf("5"), parser.args)
    }

    @Test
    fun `test negative number after option with assignment`() {
        val parser = CommandLineParser("-level=-5").parse()
        assertEquals(mapOf("level" to "-5"), parser.options)
        assertTrue(parser.args.isEmpty())
    }
}
