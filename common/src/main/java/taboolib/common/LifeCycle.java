package taboolib.common;

/**
 * TabooLib
 * taboolib.common.LifeCycle
 *
 * @author sky
 * @since 2021/6/28 10:28 下午
 */
public enum LifeCycle {

    /** 插件初始化（静态代码块被执行时）时 **/
    CONST,

    /** 插件主类被实例化时 **/
    INIT,

    /** 插件加载时 **/
    LOAD,

    /** 插件启用时 **/
    ENABLE,

    /** 服务器完全启动（调度器启动）时 **/
    ACTIVE,

    /** 插件卸载时 **/
    DISABLE
}
