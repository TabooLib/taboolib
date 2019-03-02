package com.ilummc.tlib.annotations.db;

public @interface Database {

    boolean sharedPool() default true;

    int poolSize() default 8;

    Class<?> configClass();

}
