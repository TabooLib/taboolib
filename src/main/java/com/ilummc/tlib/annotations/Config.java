package com.ilummc.tlib.annotations;

public @interface Config {

    String name() default "config.yml";

    boolean fromJar() default true;

    boolean saveOnExit() default false;

    boolean readOnly() default true;

    boolean fixUnicode() default true;

    String charset() default "UTF-8";

    boolean listenChanges() default false;

}
