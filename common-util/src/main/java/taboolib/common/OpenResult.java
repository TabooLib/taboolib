package taboolib.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.Reflex;

public class OpenResult {

    private final boolean successful;
    private final Object value;

    public OpenResult(boolean successful, @Nullable Object value) {
        this.successful = successful;
        this.value = value;
    }

    /**
     * 是否成功
     *
     * @return boolean 是否成功
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * 是否失败
     *
     * @return boolean 是否失败
     */
    public boolean isFailed() {
        return !successful;
    }

    /**
     * 获取返回值
     *
     * @return Object 返回值
     */
    @Nullable
    public Object getValue() {
        return value;
    }

    /**
     * 创建一个成功的 OpenResult
     *
     * @return OpenResult
     */
    @NotNull
    public static OpenResult successful() {
        return new OpenResult(true, null);
    }

    /**
     * 创建一个带有返回值的成功的 OpenResult
     *
     * @param value 返回值
     * @return OpenResult
     */
    @NotNull
    public static OpenResult successful(@Nullable Object value) {
        return new OpenResult(true, value);
    }

    /**
     * 创建一个失败的 OpenResult
     *
     * @return OpenResult
     */
    @NotNull
    public static OpenResult failed() {
        return new OpenResult(false, null);
    }

    /**
     * 从其他插件的 OpenResult 转换为当前插件的 OpenResult
     */
    public static OpenResult cast(Object source) {
        Object successful = Reflex.Companion.getLocalProperty(source, "successful");
        Object value = Reflex.Companion.getLocalProperty(source, "value");
        return new OpenResult(Boolean.TRUE.equals(successful), value);
    }
}