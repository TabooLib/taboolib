package io.izzel.taboolib.module.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author sky
 * @Since 2018-09-14 23:45
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TPacket {

    Type type();

    enum Type {

        SEND, RECEIVE
    }
}