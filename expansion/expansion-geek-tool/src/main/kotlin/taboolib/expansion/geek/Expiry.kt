package taboolib.expansion.geek

import java.text.SimpleDateFormat

/**
 * 作者: 老廖
 * 时间: 2022/10/2
 */
object Expiry {

    private val dRegex = Regex("(d|D|天)")
    private val hRegex = Regex("(h|H|小时)")
    private val mRegex = Regex("(m|M|分钟)")
    private val sRegex = Regex("(s|S|秒)")

    private val format = SimpleDateFormat("yyyy年 MM月 dd日 HH:mm:ss")

    /**
     * @param reverse 是否反向获取
     *
     * 列如：
     * 传入格式 - 1652346294738
     * 获得格式 - 00d 00h 00m 00s
     */
    fun getExpiryDate(millis: Long, reverse: Boolean = false): String {
        val times = if (reverse) millis else (millis - System.currentTimeMillis()) / 1000
        val dd = times / 60 / 60 / 24
        val hh = times / 60 / 60 % 24
        val mm = times / 60 % 60
        val ss = times % 60
        if (dd <= 0 && hh <= 0 && mm <= 0) {
            return if (ss <= 0) "0" else "$ss 秒"
        }
        if (dd <= 0 && hh <= 0) {
            return "$mm 分钟 $ss 秒"
        }
        if (dd <= 0) {
            return "$hh 小时 $mm 分钟 $ss 秒"
        }
        return "$dd 天 $hh 小时 $mm 分钟 $ss 秒"
    }


    /**
     * @param expire 是否获取到期时间
     *
     * 获取到期时间戳
     * 列如：
     * 传入格式 - 1d1h21m60s 或者 1d 30s 或者 30s
     * 返回当前时间 + 传入时间的时间戳
     * 获得格式 - 1652346294738
     */
    fun getExpiryMillis(timeData: String, expire: Boolean = true): Long {
        val var200 = timeData.replace(dRegex, "d ").replace(hRegex, "h ").replace(mRegex, "m ").replace(sRegex, "s ")
        var dd: Long = 0
        var hh: Long = 0
        var mm: Long = 0
        var ss: Long = 0
        var200.split(" ").forEach { value ->
            when {
                value.contains(dRegex) -> dd = value.filter { it.isDigit() }.toLong()
                value.contains(hRegex) -> hh = value.filter { it.isDigit() }.toLong()
                value.contains(mRegex) -> mm = value.filter { it.isDigit() }.toLong()
                value.contains(sRegex) -> ss = value.filter { it.isDigit() }.toLong()
            }
        }
        return if (expire) getExpiry(dd, hh, mm, ss) + System.currentTimeMillis() else getExpiry(dd, hh, mm, ss) / 1000
    }

    private fun getExpiry(dd: Long, hh: Long, mm: Long, ss: Long): Long {
        val d = dd * 24 * 60 * 60
        val h = hh * 60 * 60
        val m = mm * 60
        val var1 = d + h + m + ss
        return var1 * 1000
    }
}