package taboolib.module.incision.annotation

/**
 * 版本范围过滤注解。
 *
 * 用途：
 * 让同一份 advice 只在指定运行环境版本区间内注册，常用于 NMS、平台差异或兼容层切换。
 *
 * 使用：
 * 把它与任意 advice 注解同时标在同一方法上。扫描阶段会先用 [matcher] 解析当前版本，
 * 再判断它是否落在 [`start`, `end`] 闭区间内。
 *
 * 效果：
 * - `start = ""` 表示无下界
 * - `end = ""` 表示无上界
 * - 两端都为空时，等价于始终生效
 *
 * 比较规则：
 * 版本字符串采用 Minecraft 风格 dot-decimal，例如 `1.20`、`1.20.4`、`1.21.1`，
 * 按段数值比较，缺失段视为 0。
 *
 * 局限：
 * 1. 默认 matcher 读取的是 Minecraft/NMS 语义；若你的版本来源不同，应显式提供 [matcher]。
 * 2. `matcher` 解析失败时会回退到 Noop matcher，这通常意味着该 advice 不会按你预期筛选。
 *
 * @see taboolib.module.incision.api.VersionMatcher
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Version(
    /** 起始版本（含）；空字符串表示无下界。 */
    val start: String = "",
    /** 结束版本（含）；空字符串表示无上界。 */
    val end: String = "",
    /** 版本来源匹配器实现类的 FQCN；留空时使用默认的 Minecraft matcher。 */
    val matcher: String = "",
)
