package taboolib.common5;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;
import taboolib.common.platform.Ghost;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 文件改动监听工具
 *
 * @author lzzelAliz
 */
@Ghost
public class FileWatcher {

    /**
     * 文件监听器单例
     */
    public final static FileWatcher INSTANCE = new FileWatcher(500);

    /**
     * 定时执行服务，用于定期检查文件变动
     */
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
            1,
            new BasicThreadFactory.Builder()
                    .namingPattern("TConfigWatcherService-%d")
                    .uncaughtExceptionHandler((t, e) -> e.printStackTrace())
                    .build()
    );

    /**
     * 当前已注册的文件监听器列表
     */
    private final Map<File, FileListener> fileListenerMap = new ConcurrentHashMap<>();

    /**
     * 目录级 WatchKey 引用计数。
     * <p>
     * {@link Path#register} 对同一目录返回同一个 {@link WatchKey}，因此监听同目录下的多个文件会共享一个 key。
     * 若在移除某个监听器时直接 cancel 该 key，同目录下其余监听器会一并失效，
     * 所以这里按目录计数，只有最后一个监听器离开时才真正 cancel。
     */
    private final Map<Path, DirectoryRegistration> directoryRegistrations = new ConcurrentHashMap<>();

    /**
     * 共享的 WatchService 实例
     */
    private final WatchService watchService;

    /**
     * 监听器是否已经释放
     */
    private final AtomicBoolean released = new AtomicBoolean(false);

    public FileWatcher(int interval) {
        WatchService ws;
        try {
            ws = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            PrimitiveIO.warning(PrimitiveIO.t(
                "FileWatcher 初始化失败，文件自动重载功能不可用: " + e.getMessage(),
                "FileWatcher failed to initialize, auto-reload is unavailable: " + e.getMessage()
            ));
            ws = null;
        }
        this.watchService = ws;
        if (this.watchService != null) {
            this.executorService.scheduleAtFixedRate(() -> {
                try {
                    WatchKey key;
                    while ((key = watchService.poll()) != null) {
                        WatchKey finalKey = key;
                        key.pollEvents().forEach(event -> {
                            if (event.context() instanceof Path) {
                                Path changedPath = (Path) event.context();
                                // 通过 WatchKey 获取监听的目录，构建完整路径
                                Path watchedPath = (Path) finalKey.watchable();
                                Path fullChangedPath = watchedPath.resolve(changedPath).toAbsolutePath().normalize();
                                fileListenerMap.forEach((file, listener) -> {
                                    try {
                                        listener.handleEvent(fullChangedPath);
                                    } catch (Throwable ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            }
                        });
                        if (!key.reset()) {
                            // 目录已不可访问，移除其上所有监听器并释放目录引用
                            fileListenerMap.forEach((file, listener) -> {
                                if (listener.watchKey == finalKey && fileListenerMap.remove(file, listener)) {
                                    listener.cancel();
                                }
                            });
                            directoryRegistrations.values().removeIf(it -> it.watchKey == finalKey);
                        }
                    }
                } catch (ClosedWatchServiceException ignored) {
                    // 正常释放时关闭 WatchService，会终止后续轮询
                }
            }, 1000, interval, TimeUnit.MILLISECONDS);
            // 注册关闭回调
            TabooLib.registerLifeCycleTask(LifeCycle.DISABLE, 0, this::release);
        } else {
            this.executorService.shutdownNow();
        }
    }

    /**
     * 添加简单的文件监听器
     *
     * @param file     要监听的文件
     * @param runnable 文件变动时执行的操作
     */
    public void addSimpleListener(File file, Consumer<File> runnable) {
        addSimpleListener(file, runnable, false);
    }

    /**
     * 添加简单的文件监听器
     *
     * @param file           要监听的文件
     * @param runnable       文件变动时执行的操作
     * @param runImmediately 是否在添加监听器时立即执行一次
     */
    public void addSimpleListener(File file, Consumer<File> runnable, boolean runImmediately) {
        if (watchService == null || released.get()) {
            return;
        }
        if (runImmediately) {
            runnable.accept(file);
        }
        try {
            File canonicalFile = file.getCanonicalFile();
            FileListener listener = new FileListener(canonicalFile, runnable, this);
            FileListener previous = fileListenerMap.put(canonicalFile, listener);
            if (previous != null) {
                previous.cancel();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册目录监听并递增引用计数，同一目录复用同一个 WatchKey。
     */
    private WatchKey retainDirectory(Path directory) throws IOException {
        DirectoryRegistration registration = directoryRegistrations.compute(directory, (key, existing) -> {
            if (existing != null && existing.watchKey.isValid()) {
                existing.references++;
                return existing;
            }
            return new DirectoryRegistration(null, 1);
        });
        // 首次注册（或原有 key 已失效）时补上真正的 WatchKey
        if (registration.watchKey == null) {
            synchronized (registration) {
                if (registration.watchKey == null) {
                    registration.watchKey = directory.register(
                            watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY
                    );
                }
            }
        }
        return registration.watchKey;
    }

    /**
     * 递减目录引用计数，计数归零时才真正 cancel WatchKey。
     */
    private void releaseDirectory(Path directory) {
        directoryRegistrations.computeIfPresent(directory, (key, existing) -> {
            if (--existing.references > 0) {
                return existing;
            }
            if (existing.watchKey != null) {
                existing.watchKey.cancel();
            }
            return null;
        });
    }

    /**
     * 目录注册记录
     */
    private static class DirectoryRegistration {

        volatile WatchKey watchKey;
        int references;

        DirectoryRegistration(WatchKey watchKey, int references) {
            this.watchKey = watchKey;
            this.references = references;
        }
    }

    /**
     * 移除文件的监听器
     *
     * @param file 要移除监听的文件
     */
    public void removeListener(File file) {
        File canonicalFile;
        try {
            canonicalFile = file.getCanonicalFile();
        } catch (IOException ignored) {
            canonicalFile = file.getAbsoluteFile();
        }
        FileListener listener = fileListenerMap.remove(canonicalFile);
        if (listener != null) {
            listener.cancel();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (!released.compareAndSet(false, true)) {
            return;
        }
        fileListenerMap.values().forEach(FileListener::cancel);
        fileListenerMap.clear();
        directoryRegistrations.clear();
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
        }
        executorService.shutdownNow();
    }

    /**
     * 监听器对象
     */
    static class FileListener {

        final File file;
        final Consumer<File> callback;
        final FileWatcher fileWatcher;
        final WatchKey watchKey;
        final Path directory;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        FileListener(File file, Consumer<File> callback, FileWatcher fileWatcher) throws IOException {
            this.file = file.getCanonicalFile();
            this.callback = callback;
            this.fileWatcher = fileWatcher;
            if (this.file.isDirectory()) {
                this.directory = this.file.toPath();
            } else {
                this.directory = this.file.getParentFile().toPath();
            }
            this.watchKey = fileWatcher.retainDirectory(this.directory);
        }

        public void handleEvent(Path fullChangedPath) {
            Path watchedFile = file.toPath().toAbsolutePath().normalize();
            Path changedFile = fullChangedPath.toAbsolutePath().normalize();
            // 监听目录
            if (file.isDirectory()) {
                if (changedFile.startsWith(watchedFile)) {
                    callback.accept(changedFile.toFile());
                }
            }
            // 监听文件。删除事件发生时目标文件已不存在，Files.isSameFile 会失败，
            // 因此先比较规范化路径，再用 isSameFile 兼容符号链接。
            else if (changedFile.equals(watchedFile) || isSameFile(changedFile, watchedFile)) {
                callback.accept(changedFile.toFile());
            }
        }

        public boolean isSameFile(Path path1, Path path2) {
            try {
                return Files.isSameFile(path1, path2);
            } catch (IOException e) {
                return false;
            }
        }

        /**
         * 释放该监听器占用的目录引用。多次调用是幂等的。
         */
        public void cancel() {
            if (cancelled.compareAndSet(false, true)) {
                fileWatcher.releaseDirectory(directory);
            }
        }
    }
}
