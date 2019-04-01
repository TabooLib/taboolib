package com.ilummc.tlib.inject;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author lzzelAliz
 */
public class TConfigWatcher {

    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("TConfigWatcherService-%d").build());
    private final Map<WatchService, Triple<File, Object, Consumer<Object>>> map = new HashMap<>();

    public TConfigWatcher() {
        service.scheduleAtFixedRate(() -> {
            synchronized (map) {
                map.forEach((service, triple) -> {
                    WatchKey key;
                    while ((key = service.poll()) != null) {
                        for (WatchEvent<?> watchEvent : key.pollEvents()) {
                            if (triple.getLeft().getName().equals(Objects.toString(watchEvent.context()))) {
                                triple.getRight().accept(triple.getMiddle());
                            }
                        }
                        key.reset();
                    }
                });
            }
        }, 1000, 100, TimeUnit.MILLISECONDS);
    }

    public void addSimpleListener(File file, Runnable runnable) {
        addListener(file, null, obj -> runnable.run());
    }

    public void addOnListen(File file, Object obj, Consumer<Object> consumer) {
        try {
            WatchService service = FileSystems.getDefault().newWatchService();
            file.getParentFile().toPath().register(service, StandardWatchEventKinds.ENTRY_MODIFY);
            map.putIfAbsent(service, Triple.of(file, obj, consumer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void addListener(File file, T obj, Consumer<T> consumer) {
        addOnListen(file, obj, (Consumer<Object>) consumer);
    }

    public void removeListener(File file) {
        synchronized (map) {
            map.entrySet().removeIf(entry -> {
                if (entry.getValue().getLeft().equals(file)) {
                    try {
                        entry.getKey().close();
                    } catch (IOException ignored) {
                    }
                    return true;
                }
                return false;
            });
        }
    }

    public void unregisterAll() {
        service.shutdown();
        map.forEach((service, pair) -> {
            try {
                service.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
