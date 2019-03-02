package com.ilummc.eagletdl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

class SplitDownload implements Runnable {

    private URL url;
    long startIndex, endIndex;
    private File target;
    private EagletTask task;

    private transient long currentIndex, lastUpdateTime = System.currentTimeMillis(), tmpStart;
    private transient int retry = 0;
    private transient boolean complete;

    SplitDownload(URL url, long startIndex, long endIndex, File dest, EagletTask task) {
        this.url = url;
        tmpStart = this.startIndex = this.currentIndex = startIndex;
        this.endIndex = endIndex;
        target = dest;
        this.task = task;
    }

    void setStartIndex(long index) {
        this.tmpStart = index;
    }

    long getLastUpdateTime() {
        return lastUpdateTime;
    }

    long getCurrentIndex() {
        return currentIndex;
    }

    int getRetry() {
        return retry;
    }

    boolean isComplete() {
        return complete || currentIndex == endIndex + 1;
    }

    @Override
    public void run() {
        try {
            complete = false;
            currentIndex = tmpStart;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // set the connection properties
            task.httpHeader.forEach(connection::addRequestProperty);
            connection.setRequestMethod(task.requestMethod);
            connection.setConnectTimeout(task.connectionTimeout);
            connection.setReadTimeout(task.readTimeout);
            // set the download range
            connection.setRequestProperty("Range", "bytes=" + tmpStart + "-" + endIndex);
            connection.connect();
            // if response code not equals 206, it means that the server do not support multi thread downloading
            if (connection.getResponseCode() == 206) {
                RandomAccessFile file = new RandomAccessFile(target, "rwd");
                file.seek(tmpStart);
                byte[] buf = new byte[1024];
                int len;
                try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
                    while ((len = stream.read(buf)) > 0) {
                        file.write(buf, 0, len);
                        lastUpdateTime = System.currentTimeMillis();
                        currentIndex += len;
                        // some mysterious error occurred while downloading
                        if (currentIndex >= endIndex + 2) {
                            currentIndex = tmpStart;
                            lastUpdateTime = 0;
                            retry++;
                            return;
                        }
                    }
                    complete = true;
                }
                file.close();
            } else {
                throw new DoNotSupportMultipleThreadException();
            }
        } catch (Exception e) {
            task.onError.handle(new ErrorEvent(e, task));
            retry++;
        }
    }
}
