package taboolib.common;

import org.jetbrains.annotations.NotNull;

/**
 * TabooLib
 * taboolib.common.OpenReceiver
 *
 * @author sky
 * @since 2021/7/4 8:30 下午
 */
public interface OpenReceiver {

    /**
     * 注册开放接口
     *
     * @param name 名字
     * @param any  序列化类
     * @param args 参数
     * @return boolean 是否注册成功
     */
    boolean register(@NotNull String name, @NotNull byte[] any, @NotNull String[] args);

    /**
     * 注销开放接口
     *
     * @param name 名字
     * @param any  序列化类
     * @param args 参数
     * @return boolean 是否注销成功
     */
    boolean unregister(@NotNull String name, @NotNull byte[] any, @NotNull String[] args);
}
