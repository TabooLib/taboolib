package io.izzel.taboolib.module.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据包监听器注解
 *
 * @Author sky
 * @Since 2018-09-14 23:45
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TPacket {

    /**
     * 监听器类型
     */
    Type type();

    enum Type {

        /**
         * 从服务端向客户端发送
         */
        SEND,

        /**
         * 从客户端向服务端发送
         */
        RECEIVE
    }
}