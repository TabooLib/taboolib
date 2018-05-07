package com.ilummc.tlib.db;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.resources.TLocale;
import org.javalite.activejdbc.Base;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class Pool extends ThreadPoolExecutor {

    private static final AtomicInteger number = new AtomicInteger(1);

    private static final Pool singleton = new Pool();

    private final TLibDataSource dataSource;

    private Pool() {
        super(TLib.getTLib().getConfig().getMaximumPoolSize(),
                TLib.getTLib().getConfig().getMaximumPoolSize(),
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            dataSource = new TLibDataSource();
            this.setThreadFactory(r -> new Thread(() -> {
                Base.open(dataSource.getDataSource());
                r.run();
            }, "TabooLib-DbPool-" + number.getAndIncrement()));
            prestartAllCoreThreads();
            TLocale.sendToConsole("DATABASE.CONNECTION-ESTABLISHED", dataSource.getDataSource().getConnection().getMetaData().getDatabaseProductName(),
                    String.valueOf(TLib.getTLib().getConfig().getMaximumPoolSize()));
        } catch (Exception e) {
            TLocale.sendToConsole("DATABASE.CONNECTION-ERROR", e.toString());
            throw new RuntimeException();
        }
    }

    public static void run(Runnable runnable) {
        instance().execute(runnable);
    }

    public static void init() {

    }

    public static void unload() {
        instance().dataSource.disconnect();
        instance().shutdown();
    }

    public static Pool instance() {
        return singleton;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (t != null) Base.close();
    }

}
