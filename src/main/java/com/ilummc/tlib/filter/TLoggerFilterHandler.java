package com.ilummc.tlib.filter;

import java.util.logging.LogRecord;

/**
 * @Author 坏黑
 * @Since 2018-11-29 11:42
 */
public abstract class TLoggerFilterHandler {

    abstract public boolean isLoggable(LogRecord e);

}
