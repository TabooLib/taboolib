package taboolib.expansion.geek

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat

class ExpIryBuilder(

    /**
     * # 入参
     * 1d1h30m42s
     * 1天4时30秒10m
     */
    @SerializedName("t")
    val timer: String,

    /**
     * 时间戳计时方法，默认倒计时
     */
    @SerializedName("i")
    val isDown: Boolean = true

) {

    @SerializedName("m")
    var millis: Long = parseStringTimerToLong(timer)
        private set


    /**
     * 获取格式化的显示时间
     * @param format 可自定义的返回格式 默认 yyyy年 MM月 dd日 HH:mm:ss
     * @return 格式化的显示时间
     */
    fun getFormat(format: SimpleDateFormat = formatsTime): String {
        return format.format(millis * 1000)
    }

    /**
     * 获取 timedata 的格式化单位
     * 默认正计时 基于初始化语言选择后辍
     * @return 获得格式 - 00d 00h 00m 00s
     */
    fun getExpiryFormat(): String {
        return getExpiryFormat(millis)
    }

    /**
     * 根据计时种类更新时间戳
     * 适合有独立线程即时更新的
     */
    fun autoUpdate(time: Long = 1): Boolean {
        if (isDown) {
            if (this.millis > 0) {
                this.millis -= time
            }
        } else {
            this.millis += time
        }
        return this.millis > 0
    }

    /**
     * 自然时间减少、增加
     */
    fun update(): ExpIryBuilder {
        if (isDown) {
            this.millis -= this.millis - (System.currentTimeMillis() / 1000)
            if (this.millis < 0) {
                this.millis = 0
            }
        } else {
            this.millis += (System.currentTimeMillis() / 1000) - this.millis
        }
        // 不能负数
        if (this.millis < 0) {
            this.millis = 0
        }
        this.millis -= System.currentTimeMillis() / 1000
        return this
    }

    /**
     * 追加时间值
     */
    fun addMillis(time: Long): ExpIryBuilder {
        this.millis += time
        return this
    }

    /**
     * 减少时间值
     */
    fun takeMillis(time: Long): ExpIryBuilder {
        if (this.millis > 0) {
            this.millis -= time
        }
        return this
    }

    fun setMillis(time: Long): ExpIryBuilder {
        this.millis = time
        return this
    }

    fun toJsonText(): String {
         return gsonBuilder
            .create()
            .toJson(this)
    }

    companion object {

        private val expiryRegex: Regex by lazy { Regex("\\d+?(?i)(d|h|m|s|天|时|分|秒)\\s?") }

        private val formatsTime: SimpleDateFormat by lazy { SimpleDateFormat("yyyy年 MM月 dd日 HH:mm:ss") }

        private val gsonBuilder: GsonBuilder by lazy { GsonBuilder().setExclusionStrategies(
            object : ExclusionStrategy {
                override fun shouldSkipField(f: FieldAttributes): Boolean {
                    return f.getAnnotation(Expose::class.java) != null
                }
                override fun shouldSkipClass(clazz: Class<*>): Boolean {
                    return clazz.getAnnotation(Expose::class.java) != null
                }
            })
        }

        fun getExpiryFormat(time: Long): String {
            val builder: StringBuilder = java.lang.StringBuilder()
            val dd = time / 60 / 60 / 24
            val hh = time / 60 / 60 % 24
            val mm = time / 60 % 60
            val ss = time % 60
            if (dd > 0) builder.append(dd).append("天 ")
            if (hh > 0) builder.append(hh).append("时 ")
            if (mm > 0) builder.append(mm).append("分 ")
            if (ss > 0) builder.append(ss).append("秒 ")
            return builder.toString()
        }

        fun parseStringTimerToLong(time: String): Long {
            if (time.isEmpty()) return -1
            var timer: Long = 0
            expiryRegex.findAll(time).forEach {
                timer += it.groupValues[0].substringBefore(it.groupValues[1]).toLong()
            }
            return timer
        }

        fun parseStringTimerToMap(time: String): Map<String, Long> {
            if (time.isEmpty()) return emptyMap()
            return mutableMapOf<String, Long>().apply {
                expiryRegex.findAll(time).forEach {
                    this[it.groupValues[1]] = it.groupValues[0].substringBefore(it.groupValues[1]).toLong()
                }
            }
        }
    }
}

/**
 * 通过字符串获取 ExpIryBuilder 更好的管理期限
 * @param isDown 是否倒计时
 */
fun String.toExpiryBuilder(isDown: Boolean): ExpIryBuilder {
    if (isEmpty()) {
        error("字符串为空...")
    }
    return ExpIryBuilder(this, isDown)
}