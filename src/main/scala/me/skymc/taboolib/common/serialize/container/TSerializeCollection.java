package me.skymc.taboolib.common.serialize.container;

import me.skymc.taboolib.common.serialize.TSerializerElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @Author sky
 * @Since 2018-10-05 12:11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TSerializeCollection {

    Class<? extends Collection> type() default ArrayList.class;

    Class<? extends TSerializerElement> element() default TSerializerElement.class;

}