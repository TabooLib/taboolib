package taboolib.common;

import org.jetbrains.annotations.NotNull;

/**
 * TabooLib
 * taboolib.common.OpenReceiver
 *
 * @author sky
 * @since 2021/7/4 8:30 下午
 */
public interface OpenListener {

    /**
     * 注册开放接口
     *
     * @param name 名字
     * @param data 数据
     * @return boolean 是否注册成功
     */
    OpenResult call(@NotNull String name, @NotNull Object[] data);
}
