package com.ilummc.tlib.inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Triple;

public class TConfigWatcher {

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    private Map<WatchService, Triple<File, Object, Consumer<Object>>> map = new HashMap<>();

    public TConfigWatcher() {
        service.scheduleAtFixedRate(() -> {
            map.forEach((service, triple) -> {
                WatchKey key;
                while ((key = service.poll()) != null) {
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        if (triple.getLeft().getName().equals(Objects.toString(watchEvent.context())))
                            triple.getRight().accept(triple.getMiddle());
                    }
                    key.reset();
                }
            });
        }, 1000, 100, TimeUnit.MILLISECONDS);
    }

    public void addOnListen(File file, Object obj, Consumer<Object> consumer) {
        try {
            WatchService service = FileSystems.getDefault().newWatchService();
            file.getParentFile().toPath().register(service, StandardWatchEventKinds.ENTRY_MODIFY);
            map.put(service, Triple.of(file, obj, consumer));
        } catch (IOException e) {
            e.printStackTrace();
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
