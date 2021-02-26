package io.izzel.taboolib.module.command.base;

import org.bukkit.permissions.PermissionDefault;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主命令注解
 * 用于声明 BaseMainCommand 子类并进行自动注册
 *
 * @author sky
 * @since 2018-08-23 20:34
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseCommand {

    /**
     * @return 命令名称，默认启用强制注册，替换冲突命令
     */
    String name();

    /**
     * @return 命令权限
     */
    String permission() default "";

    /**
     * @return 缺少权限提示
     */
    String permissionMessage() default "";

    /**
     * @return 默认权限设置
     */
    PermissionDefault permissionDefault() default PermissionDefault.OP;

    /**
     * @return 别名，别名不会被强制注册
     */
    String[] aliases() default {};

    /**
     * @return Bukkit 命令描述，不会在 BaseMainCommand 构建的帮助列表中显示
     */
    String description() default "";

    /**
     * @return Bukkit 使用方法，不会在 BaseMainCommand 构建的帮助列表中显示
     */
    String usage() default "";

}