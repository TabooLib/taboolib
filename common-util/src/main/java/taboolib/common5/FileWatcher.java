package taboolib.common5;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.Triple;
import taboolib.common.Isolated;
import taboolib.common.LifeCycle;
import taboolib.common.env.RuntimeDependency;
import taboolib.common.exceptions.Exceptions;
import taboolib.common.platform.Awake;
import taboolib.common.platform.Releasable;
import taboolib.common.platform.SkipTo;
import taboolib.common.util.Closeables;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 文件改动监听工具
 *
 * @author lzzelAliz
 */
@Awake
@SkipTo(LifeCycle.ENABLE)
@Isolated
@RuntimeDependency(value = "!org.apache.commons:commons-lang3:3.5", test = "!org.apache.commons.lang3.concurrent.BasicThreadFactory")
public class FileWatcher implements Releasable {

    public final static FileWatcher INSTANCE = new FileWatcher();

    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("TConfigWatcherService-%d").build());
    private final Map<WatchService, Triple<File, Object, Consumer<Object>>> map = new HashMap<>();

    public FileWatcher() {
        Runnable command = () -> {
            synchronized (map) {
                map.entrySet().stream()
                        .filter(entry -> entry.getKey() != null)
                        .forEach(entry -> {
                            WatchService service = entry.getKey();
                            File file = entry.getValue().getLeft();
                            Object obj = entry.getValue().getMiddle();
                            Consumer<Object> consumer = entry.getValue().getRight();

                            WatchKey key;

                            while (true) {
                                key = service.poll();
                                if (key == null) { break; }

                                key.pollEvents().stream()
                                        .map(event -> file.getName().equals(Objects.toString(event.context())))
                                        .forEach(ignored -> consumer.accept(obj));

                                key.reset();
                            }
                        });
            }
        };

        service.scheduleAtFixedRate(command, 1000, 100, TimeUnit.MILLISECONDS);
    }

    public void addSimpleListener(File file, Runnable runnable) {
        addSimpleListener(file, runnable, false);
    }

    public void addSimpleListener(File file, Runnable runnable, boolean runFirst) {
        if (runFirst) { runnable.run(); }
        Exceptions.runCatching(() -> addListener(file, null, obj -> runnable.run()));
    }

    public void addOnListen(File file, Object obj, Consumer<Object> consumer) {
        try {
            WatchService service = FileSystems.getDefault().newWatchService();
            file.getParentFile().toPath().register(service, StandardWatchEventKinds.ENTRY_MODIFY);
            map.putIfAbsent(service, Triple.of(file, obj, consumer));
        } catch (Throwable ignored) {}
    }

    @SuppressWarnings("unchecked")
    public <T> void addListener(File file, T obj, Consumer<T> consumer) {
        addOnListen(file, obj, (Consumer<Object>) consumer);
    }

    public boolean hasListener(File file) {
        synchronized (map) {
            return map.values().stream().anyMatch(t -> t.getLeft().getPath().equals(file.getPath()));
        }
    }

    public void runListener(File file) {
        synchronized (map) {
            map.values().stream().filter(t -> t.getLeft().getPath().equals(file.getPath())).forEach(f -> f.getRight().accept(null));
        }
    }

    public void removeListener(File file) {
        synchronized (map) {
            map.entrySet().stream()
                    .filter(entry -> entry.getValue().getLeft().getPath().equals(file.getPath()))
                    .peek(entry -> Closeables.closeSafely(entry.getKey()))
                    .forEach(entry -> map.remove(entry.getKey(), entry.getValue()));
        }
    }

    public void unregisterAll() {
        service.shutdown();
        map.forEach((service, pair) -> Closeables.closeSafely(service));
    }

    @Override
    public void release() {
        unregisterAll();
    }
}
