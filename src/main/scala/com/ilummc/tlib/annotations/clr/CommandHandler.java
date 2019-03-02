package com.ilummc.tlib.annotations.clr;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CommandHandlers.class)
public @interface CommandHandler {

    /**
     * Name of the command
     *
     * @return Name of the command
     */
    String value();

}
