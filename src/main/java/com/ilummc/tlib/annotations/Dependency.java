package com.ilummc.tlib.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Dependencies.class)
public @interface Dependency {

    enum Type {PLUGIN, LIBRARY}

    Type type();

    String[] args();

}
