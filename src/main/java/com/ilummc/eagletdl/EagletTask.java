package com.ilummc.eagletdl;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class EagletTask {

    private ReentrantLock lock = new ReentrantLock();

    Map<String, String> httpHeader = new ConcurrentHashMap<>();

    private URL url;

    EagletHandler<ErrorEvent> onError = event -> event.getException().printStackTrace();

    private EagletHandler<StartEvent> onStart;

    private EagletHandler<CompleteEvent> onComplete;

    private EagletHandler<ConnectedEvent> onConnected;

    private EagletHandler<ProgressEvent> onProgress;

    private Proxy proxy;

    private String md5, sha1, sha256;

    String requestMethod = "GET";

    private int threadAmount = 1;

    int connectionTimeout = 7000;
    int readTimeout = 7000;
    int maxRetry = 3;

    private File dest;

    private transient boolean running = false;
    private transient long contentLength, maxBlockingTime = 7000;
    private transient ExecutorService executorService;
    private transient Thread monitor;

    public EagletTask() {
    }

    /**
     * Stop this task forcefully, and the target file will not be removed.
     */
    public void stop() {
        executorService.shutdownNow();
        monitor.interrupt();
    }

    /**
     * Start the download file
     * <p>
     * 开始下载文件
     */
    public EagletTask start() {
        // create thread pool for download
        executorService = Executors.newFixedThreadPool(threadAmount);
        // check if is already running
        if (running) {
            throw new AlreadyStartException();
        }
        // start the monitor thread
        monitor = new Thread(() -> {
            lock.lock();
            // fire a new start event
            if (onStart != null) {
                onStart.handle(new StartEvent(this));
            }
            try {
                // create the target file
                if (!dest.exists()) {
                    dest.createNewFile();
                }
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // set the connection properties
                httpHeader.forEach(connection::addRequestProperty);
                connection.setRequestMethod(requestMethod);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.connect();
                contentLength = connection.getContentLengthLong();
                // fire a new connected event
                // contains connection properties
                if (onConnected != null) {
                    onConnected.handle(new ConnectedEvent(contentLength, this));
                }
                // if this is an unknown length task
                if (contentLength == -1 || threadAmount == 1) {
                    // pass the connection instance to this new thread
                    SingleThreadDownload download = new SingleThreadDownload(connection, dest, this);
                    executorService.execute(download);
                    long last = 0;
                    do {
                        Thread.sleep(1000);
                        // check the progress
                        long progress = download.getCurrentProgress();
                        // fire a new progress event
                        if (onProgress != null) {
                            onProgress.handle(new ProgressEvent(progress - last < 0 ? 0 : progress - last, this, ((double) progress) / Math.max((double) contentLength, 0D)));
                        }
                        last = progress;
                        // check complete
                    } while (last != contentLength && !download.isComplete());
                    // close the thread pool, release resources
                    executorService.shutdown();
                    // change the running flag to false
                    running = false;
                } else {
                    List<SplitDownload> splitDownloads = new ArrayList<>();
                    // Assign download task length
                    long blockSize = contentLength / threadAmount;
                    for (int threadId = 0; threadId < threadAmount; threadId++) {
                        long startIndex = threadId * blockSize;
                        long endIndex = (threadId + 1) * blockSize - 1;
                        if (threadId == (threadAmount - 1)) {
                            endIndex = contentLength - 1;
                        }
                        SplitDownload download = new SplitDownload(url, startIndex, endIndex, dest, this);
                        // Start downloading
                        executorService.execute(download);
                        splitDownloads.add(download);
                    }
                    long last = 0;
                    do {
                        Thread.sleep(1000);
                        long progress = 0;
                        // Collect download progress
                        for (SplitDownload splitDownload : splitDownloads) {
                            progress += splitDownload.getCurrentIndex() - splitDownload.startIndex;
                            // blocked then restart from current index
                            if (!splitDownload.isComplete() && System.currentTimeMillis() - splitDownload.getLastUpdateTime() > maxBlockingTime) {
                                splitDownload.setStartIndex(splitDownload.getCurrentIndex());
                                if (splitDownload.getRetry() <= maxRetry) {
                                    executorService.execute(splitDownload);
                                } else {
                                    throw new RetryFailedException(this);
                                }
                            }
                        }
                        // Fire a progress event
                        if (onProgress != null) {
                            onProgress.handle(new ProgressEvent(progress - last, this,
                                    ((double) progress) / ((double) contentLength)));
                        }
                        last = progress;
                        // check complete
                    } while (last < contentLength);
                    // close the thread pool, release resources
                    executorService.shutdown();
                    // change the running flag to false
                    running = false;
                }
                // check hash
                if (md5 != null && !md5.equalsIgnoreCase(HashUtil.md5(dest))) {
                    throw new HashNotMatchException();
                }
                if (sha1 != null && !sha1.equalsIgnoreCase(HashUtil.sha1(dest))) {
                    throw new HashNotMatchException();
                }
                if (sha256 != null && !sha256.equalsIgnoreCase(HashUtil.sha256(dest))) {
                    throw new HashNotMatchException();
                }
                if (onComplete != null) {
                    onComplete.handle(new CompleteEvent(this, true));
                }
            } catch (Exception e) {
                onError.handle(new ErrorEvent(e, this));
                executorService.shutdown();
                if (onComplete != null) {
                    onComplete.handle(new CompleteEvent(this, false));
                }
            } finally {
                lock.unlock();
            }
        }, "EagletTaskMonitor");
        monitor.start();
        return this;
    }

    public EagletTask waitUntil() {
        while (lock.tryLock()) {
            lock.unlock();
        }
        lock.lock();
        lock.unlock();
        return this;
    }

    public EagletTask waitFor(long timeout, TimeUnit unit) {
        while (lock.tryLock()) {
            lock.unlock();
        }
        try {
            lock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public EagletTask maxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    /**
     * Set the sha256 hash of the download file. Case is not sensitive.
     * <p>
     * If the hash check failed, an error event will be fired.
     *
     * @param sha256 file sha1
     * @return task instance
     */
    public EagletTask sha256(String sha256) {
        this.sha256 = sha256;
        return this;
    }

    /**
     * Set the sha1 hash of the download file. Case is not sensitive.
     * <p>
     * If the hash check failed, an error event will be fired.
     *
     * @param sha1 file sha1
     * @return task instance
     */
    public EagletTask sha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    /**
     * Set the md5 hash of the download file. Case is not sensitive.
     * <p>
     * If the hash check failed, an error event will be fired.
     *
     * @param md5 file md5
     * @return task instance
     */
    public EagletTask md5(String md5) {
        this.md5 = md5;
        return this;
    }

    /**
     * Set the max blocked time per download thread.
     * <p>
     * If the thread blocks exceeded the provided time, this thread will re-start the task.
     *
     * @param maxBlockingTime time
     * @return task instance
     */
    private EagletTask maxBlocking(long maxBlockingTime) {
        this.maxBlockingTime = maxBlockingTime;
        return this;
    }

    /**
     * Set the progress handler
     * <p>
     * This handler will be called every 1000 milli seconds.
     * <p>
     * 设置处理进度的时间监听器。该监听器的 handle 方法每秒调用一次。
     *
     * @param onProgress handler
     * @return task instance
     */
    public EagletTask setOnProgress(EagletHandler<ProgressEvent> onProgress) {
        this.onProgress = onProgress;
        return this;
    }

    /**
     * Set the download file
     *
     * @param file the file's absolute path
     * @return task instance
     */
    public EagletTask file(String file) {
        this.dest = new File(file);
        return this;
    }

    /**
     * Set the download file
     *
     * @param file the file
     * @return task instance
     */
    public EagletTask file(File file) {
        this.dest = file;
        return this;
    }

    /**
     * Set the connected handler
     * <p>
     * This will be called when the connection is established
     * <p>
     * Async call
     *
     * @param onConnected onConnected event handler
     * @return task instance
     */
    public EagletTask setOnConnected(EagletHandler<ConnectedEvent> onConnected) {
        this.onConnected = onConnected;
        return this;
    }

    /**
     * Set the read timeout, default is 7000
     *
     * @param timeout timeout
     * @return task instance
     */
    public EagletTask readTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    /**
     * Set the connection timeout, default is 7000
     *
     * @param timeout timeout
     * @return task instance
     */
    public EagletTask connectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
        return this;
    }

    /**
     * Set the request method, default is <code>GET</code>
     *
     * @param requestMethod the request method
     * @return task instance
     */
    public EagletTask requestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    /**
     * Set the complete event handler
     * <p>
     * This handler will be called when everything is complete, and the downloaded file is available
     * <p>
     * Async call
     *
     * @param onComplete the handler
     * @return task instance
     */
    public EagletTask setOnComplete(EagletHandler<CompleteEvent> onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    /**
     * Set the start handler
     * <p>
     * This handler will be called when the <code>start</code> method is called
     * <p>
     * Async call
     *
     * @param onStart the handler
     * @return task instance
     */
    public EagletTask setOnStart(EagletHandler<StartEvent> onStart) {
        this.onStart = onStart;
        return this;
    }

    /**
     * Set the network proxy
     *
     * @param proxy the proxy
     * @return task instance
     */
    public EagletTask proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Set the error handler, default is to print the stack trace
     * <p>
     * This handler will be called when an exception is thrown
     * <p>
     * Async call
     *
     * @param onError the handler
     * @return task instance
     */
    public EagletTask setOnError(EagletHandler<ErrorEvent> onError) {
        this.onError = onError;
        return this;
    }


    /**
     * Set how much thread should be used to download, default is 1
     *
     * @param i thread amount
     * @return task instance
     */
    public EagletTask setThreads(int i) {
        if (i < 1) {
            throw new RuntimeException("Thread amount cannot be zero or negative!");
        }
        threadAmount = i;
        return this;
    }

    /**
     * Set the download source
     *
     * @param url the url
     * @return task instance
     */
    public EagletTask url(URL url) {
        this.url = url;
        return this;
    }

    /**
     * Set the download source
     *
     * @param url the url
     * @return task instance
     */
    public EagletTask url(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            onError.handle(new ErrorEvent(e, this));
        }
        return this;
    }

    /**
     * Clear the http header field
     *
     * @return task instance
     */
    public EagletTask clearHeaders() {
        httpHeader.clear();
        return this;
    }

    /**
     * Set the header field of the http request
     *
     * @param key   header key
     * @param value header value
     * @return builder instance
     */
    public EagletTask header(String key, String value) {
        httpHeader.put(key, value);
        return this;
    }

}
