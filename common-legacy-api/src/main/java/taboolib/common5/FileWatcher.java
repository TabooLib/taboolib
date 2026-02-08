package taboolib.common5;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import taboolib.common.LifeCycle;
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
     * 共享的 WatchService 实例
     */
    private final WatchService watchService;

    public FileWatcher(int interval) {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.executorService.scheduleAtFixedRate(() -> {
                WatchKey key;
                while ((key = watchService.poll()) != null) {
                    key.pollEvents().forEach(event -> {
                        if (event.context() instanceof Path) {
                            Path changedPath = (Path) event.context();
                            fileListenerMap.forEach((file, listener) -> {
                                try {
                                    listener.handleEvent(changedPath);
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    });
                    key.reset();
                }
            }, 1000, interval, TimeUnit.MILLISECONDS);
            // 注册关闭回调
            TabooLib.registerLifeCycleTask(LifeCycle.DISABLE, 0, this::release);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        if (runImmediately) {
            runnable.accept(file);
        }
        try {
            fileListenerMap.put(file, new FileListener(file, runnable, this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 移除文件的监听器
     *
     * @param file 要移除监听的文件
     */
    public void removeListener(File file) {
        FileListener listener = fileListenerMap.remove(file);
        if (listener != null) {
            listener.cancel();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        executorService.shutdown();
        fileListenerMap.values().forEach(FileListener::cancel);
    }

    /**
     * 监听器对象
     */
    static class FileListener {

        final File file;
        final Consumer<File> callback;
        final FileWatcher fileWatcher;
        final WatchKey watchKey;

        FileListener(File file, Consumer<File> callback, FileWatcher fileWatcher) throws IOException {
            this.file = file.getCanonicalFile();
            this.callback = callback;
            this.fileWatcher = fileWatcher;
            Path path;
            if (this.file.isDirectory()) {
                path = this.file.toPath();
            } else {
                path = this.file.getParentFile().toPath();
            }
            watchKey = path.register(
                    fileWatcher.watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );
        }

        public void handleEvent(Path changedPath) {
            Path fullPath = file.getParentFile().toPath().resolve(changedPath);
            // 监听目录
            if (file.isDirectory()) {
                try {
                    // 使用 relativize 检查路径关系，更加准确
                    file.toPath().relativize(fullPath);
                    callback.accept(fullPath.toFile());
                } catch (IllegalArgumentException ignored) {
                    // 如果不是子路径，会抛出异常，直接忽略
                }
            }
            // 监听文件
            else if (isSameFile(fullPath, file.toPath())) {
                callback.accept(fullPath.toFile());
            }
        }

        public boolean isSameFile(Path path1, Path path2) {
            try {
                // 使用 Files.isSameFile() 判断两个路径是否指向同一个文件
                // 该方法会考虑符号链接等情况
                return Files.isSameFile(path1, path2);
            } catch (IOException e) {
                // 如果出现 IO 异常则返回 false
                return false;
            }
        }

        public void cancel() {
            watchKey.cancel();
        }
    }
}
