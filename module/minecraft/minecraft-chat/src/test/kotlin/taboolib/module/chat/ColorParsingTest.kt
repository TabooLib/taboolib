package taboolib.module.chat

import net.md_5.bungee.api.ChatColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.awt.Color

class ColorParsingTest {

    @Test
    fun `strict color parser preserves black and leading zero colors`() {
        assertEquals(0x000000, HexColor.parseColor("#000000"))
        assertEquals(0x000001, HexColor.parseColor("#000001"))
        assertEquals(0x00ff0a, HexColor.parseColor("#00ff0a"))
        assertEquals(0xffffff, HexColor.parseColor("#FFFFFF"))
    }

    @Test
    fun `strict color parser rejects malformed hex and rgb`() {
        listOf("#fff", "#00000", "#0000000", "#gggggg", "#", "##000000").forEach {
            assertNull(HexColor.parseColor(it), it)
        }
        listOf("1,2", "1,2,3,4", "255,x,255", "256,0,0", "-1,0,0", "1--2-3", "1,,3").forEach {
            assertNull(HexColor.parseColor(it), it)
        }
    }

    @Test
    fun `strict color parser accepts variable width rgb components`() {
        assertEquals(0x000000, HexColor.parseColor("0,0,0"))
        assertEquals(0x0114ff, HexColor.parseColor("1,20,255"))
        assertEquals(0xffffff, HexColor.parseColor("255-255-255"))
        assertEquals(0x0114ff, HexColor.parseColor("1 - 20 - 255"))
    }

    @Test
    fun `hex translation preserves invalid expressions and parses valid rgb`() {
        assertEquals("&{999,0,0}x", HexColor.translate("&{999,0,0}x"))
        assertEquals("&{abc,def,ghi}x", HexColor.translate("&{abc,def,ghi}x"))
        assertEquals("&{1,2", HexColor.translate("&{1,2"))
        assertEquals("${ChatColor.of(Color(1, 20, 255))}x", HexColor.translate("&{1,20,255}x"))
        assertEquals("${ChatColor.BLUE}x", HexColor.translate("&{BLUE}x"))
        assertEquals("${ChatColor.WHITE}x", HexColor.translate("&{RESET}x"))
    }
}
