package io.izzel.taboolib.client.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用该注解的成员变量
 * 将会被自动序列化
 *
 * 仅限以下类型：
 * - Number
 * - String
 * - Boolean
 * - Character
 *
 * @author sky
 * @since 2018-08-22 23:09
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketValue {

}