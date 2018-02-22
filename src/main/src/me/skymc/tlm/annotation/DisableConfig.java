package me.skymc.tlm.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
/**
 * @author sky
 * @since 2018年2月22日 下午3:59:30
 */
public @interface DisableConfig {

}
