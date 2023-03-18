package taboolib.expansion.geek.expiry

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat

class ExpIryBuilder(
    /**
     * 入参
     * 1d1h30m42s
     * 1天4时30秒10m
     */
    @SerializedName("t")
    private val time: String,
    /**
     * 时间戳计时方法，默认倒计时
     */
    @SerializedName("t2")
    private val type: ExpIryType = ExpIryType.C
) {
    @SerializedName("m")
    private var millis: Long = -0L

    @Expose
    private val ds = if (time.contains("天")) "天" else "d"
    @Expose
    private val hs = if (time.contains("时")) "时" else "h"
    @Expose
    private val ms = if (time.contains("分")) "分" else "m"
    @Expose
    private val ss = if (time.contains("秒")) "秒" else "s"
    @Expose
    private val cache: MutableMap<String, Long> = mutableMapOf<String, Long>().apply {
        if (time.isEmpty()) return@apply
        val regex = Regex("\\d+?(?i)($ds|$hs|$ms|$ss)\\s?")
        regex.findAll(time).forEach {
            this[it.groupValues[1]] = it.groupValues[0].substringBefore(it.groupValues[1]).toLong()
        }
    }
    /**
     * 防止首次初始化 millis 序列化值未更新
     */
    init {
        getMilli()
    }

    /**
     * 获取格式化的显示时间
     * @param format 可自定义的返回格式 默认 yyyy年 MM月 dd日 HH:mm:ss
     * @return 格式化的显示时间
     */
    fun getFormat(format: SimpleDateFormat = formats): String {
        return format.format(getMilli())
    }

    /**
     * 获取 timedata 的格式化单位
     * 默认正计时 基于初始化语言选择后辍
     * @return 获得格式 - 00d 00h 00m 00s
     */
    fun getExpiryFormat(): String {
        return Companion.getExpiryFormat(getMilli())
    }

    /**
     * 获取到期时间戳
     * @return timeData*1000 + 系统时间戳
     */
    fun getExpiryMillis(): Long {
        return (getMilli()*1000) + System.currentTimeMillis()
    }

    /**
     * 根据计时种类更新时间戳
     */
    fun autoUpdate(time: Long = 1): Boolean {
        if (type == ExpIryType.C) {
            if (this.millis > 0) this.millis-=time
        } else this.millis+=time
        return this.millis > 0
    }
    /**
     * 追加时间值
     */
    fun addMillis(time: Long): Long {
        this.millis+=time
        return millis
    }
    /**
     * 减少时间值
     */
    fun takeMillis(time: Long): Long {
        if (this.millis > 0) this.millis-=time
        return millis
    }
    fun setMillis(time: Long): ExpIryBuilder {
        this.millis = time
        return this
    }
    /**
     * 获取 timeData 的时间
     */
    fun getMilli(): Long {
        if (millis == -0L) millis = getExpiry()
        return millis
    }

    private fun getExpiry(): Long {
        if (this.cache.isEmpty()) return 0
        val d = cache[ds]?.let { it * 24 * 60 * 60 } ?: 0
        val h = cache[hs]?.let { it * 60 * 60 } ?: 0
        val m = cache[ms]?.let { it * 60 } ?: 0
        return d + h + m + (cache[ss] ?: 0)
    }

    fun toJsonText(): String {
         return GsonBuilder()
            .setExclusionStrategies(object : ExclusionStrategy {
                override fun shouldSkipField(f: FieldAttributes): Boolean {
                    return f.getAnnotation(Expose::class.java) != null
                }

                override fun shouldSkipClass(clazz: Class<*>): Boolean {
                    return clazz.getAnnotation(Expose::class.java) != null
                }
            })
            .create()
            .toJson(this)
    }

    companion object {
        /**
         * 格式化时间戳
         */
        private val formats by lazy { SimpleDateFormat("yyyy年 MM月 dd日 HH:mm:ss") }

        fun getFormat(time: Long): String {
            return formats.format(time)
        }
        fun getExpiryFormat(time: Long): String {
            var text = ""
            val dd = time / 60 / 60 / 24
            val hh = time / 60 / 60 % 24
            val mm = time / 60 % 60
            val ss = time % 60
            if (dd > 0) text += "${dd}天 "
            if (hh > 0) text += "${hh}小时 "
            if (mm > 0) text += "${mm}分钟 "
            if (ss > 0) text += "${ss}秒 "
            return text
        }

    }
}