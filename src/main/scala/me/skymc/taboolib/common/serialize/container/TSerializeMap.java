package me.skymc.taboolib.common.serialize.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-10-05 12:11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TSerializeMap {

    Class<? extends Map> type() default HashMap.class;

}