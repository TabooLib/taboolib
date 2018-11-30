package com.ilummc.tlib.filter.impl;

import com.google.common.collect.Lists;
import com.ilummc.tlib.filter.TLoggerFilterHandler;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import org.bukkit.command.CommandException;
import org.bukkit.event.EventException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author 坏黑
 * @Since 2018-11-29 11:42
 */
public class FilterExceptionMirror extends TLoggerFilterHandler {

    interface ArgumentsCallback {

        String[] run();
    }

    private static Pattern patternEvent = Pattern.compile("Could not pass event (.+?) to (.+?)");
    private static Pattern patternCommand = Pattern.compile("Unhandled exception executing command '(.+?)' in plugin (.+?)");

    /**
     * 判断是否为调度器异常
     */
    public boolean isScheduleException(LogRecord log) {
        return String.valueOf(log.getMessage()).contains("generated an exception");
    }

    /**
     * 是否为可捕捉异常
     */
    public boolean isValidException(Throwable throwable) {
        return throwable.getCause() != null && throwable.getCause().getStackTrace() != null && throwable.getCause().getStackTrace().length > 0;
    }

    /**
     * 向控制台打印捕捉到的异常
     *
     * @param stackTraceElements 堆栈
     * @param message            信息类型
     * @param args               信息参数
     * @return 是否成功捕捉并打印
     */
    public boolean printException(AtomicReference<Plugin> plugin, StackTraceElement[] stackTraceElements, String message, ArgumentsCallback args) {
        List<StackTraceElement> stackTraces = Lists.newLinkedList();
        for (StackTraceElement stack : stackTraceElements) {
            try {
                plugin.set(JavaPlugin.getProvidingPlugin(Class.forName(stack.getClassName())));
                if (TabooLib.isTabooLib(plugin.get()) || TabooLib.isDependTabooLib(plugin.get())) {
                    stackTraces.add(stack);
                }
            } catch (Exception ignored) {
            }
        }
        if (plugin.get() != null && (TabooLib.isTabooLib(plugin.get()) || TabooLib.isDependTabooLib(plugin.get()))) {
            TLocale.Logger.error("TFILTER.EXCEPTION-MIRROR." + message + ".HEAD", args.run());
            for (int i = 0; i < stackTraces.size(); i++) {
                StackTraceElement stack = stackTraces.get(i);
                TLocale.Logger.error("TFILTER.EXCEPTION-MIRROR." + message + ".STACK-TRACE", String.valueOf(i), stack.toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isLoggable(LogRecord e) {
        if (!Main.getInst().getConfig().getBoolean("EXCEPTION-MIRROR", true) || e.getThrown() == null) {
            return true;
        }
        // 是否为调度器异常
        if (isScheduleException(e)) {
            long time = System.currentTimeMillis();
            AtomicReference<Plugin> plugin = new AtomicReference<>();
            return !printException(plugin, e.getThrown().getStackTrace(), "SCHEDULE", () -> new String[] {plugin.get().getName(), String.valueOf(System.currentTimeMillis() - time), e.getThrown().getClass().getName(), String.valueOf(e.getThrown().getMessage())});
        }
        // 是否为其他可捕捉异常
        else if (isValidException(e.getThrown())) {
            // 事件异常
            if (e.getThrown() instanceof EventException) {
                Matcher matcher = patternEvent.matcher(e.getMessage());
                if (matcher.find()) {
                    long time = System.currentTimeMillis();
                    AtomicReference<Plugin> plugin = new AtomicReference<>();
                    return !printException(plugin, e.getThrown().getCause().getStackTrace(), "EVENT", () -> new String[] {plugin.get().getName(), String.valueOf(System.currentTimeMillis() - time), matcher.group(1), e.getThrown().getCause().getClass().getName(), String.valueOf(e.getThrown().getCause().getMessage())});
                }
            }
            // 命令异常
            else if (e.getThrown() instanceof CommandException) {
                Matcher matcher = patternCommand.matcher(e.getThrown().getMessage());
                if (matcher.find()) {
                    long time = System.currentTimeMillis();
                    AtomicReference<Plugin> plugin = new AtomicReference<>();
                    return !printException(plugin, e.getThrown().getCause().getStackTrace(), "COMMAND", () -> new String[] {plugin.get().getName(), String.valueOf(System.currentTimeMillis() - time), matcher.group(1), e.getThrown().getCause().getClass().getName(), String.valueOf(e.getThrown().getCause().getMessage())});
                }
            }
            // 其他异常
            else {
                long time = System.currentTimeMillis();
                AtomicReference<Plugin> plugin = new AtomicReference<>();
                return !printException(plugin, e.getThrown().getCause().getStackTrace(), "OTHER", () -> new String[] {plugin.get().getName(), String.valueOf(System.currentTimeMillis() - time), e.getThrown().getCause().getClass().getName(), String.valueOf(e.getThrown().getCause().getMessage())});
            }
        }
        return true;
    }
}
