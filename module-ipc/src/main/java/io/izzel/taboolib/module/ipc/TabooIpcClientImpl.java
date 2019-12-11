package io.izzel.taboolib.module.ipc;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class TabooIpcClientImpl implements TabooIpcClient {

    private volatile boolean available = false;
    private MemoryMappedFile file;
    private int id;
    private MessageBlock block;

    @Override
    public synchronized boolean connect(TabooIpcConfig config) throws Exception {
        if (available) throw new IllegalStateException();
        long size = round(config.memorySize());
        long blockSize = round(config.blockSize());
        if (blockSize < 128 || size < (blockSize << 1) || size % blockSize != 0) return false;
        try (RandomAccessFile raf = new RandomAccessFile(config.fileLocation(), "rw")) {
            raf.setLength(size);
            try (FileChannel channel = raf.getChannel()) {
                this.file = new MemoryMappedFile(channel, size);
                this.id = file.getAndAddInt(0, 1);
                if (!login(size, blockSize, config.timeout())) {
                    this.file.unmap();
                    return available = false;
                }
                return available = true;
            }
        }
    }

    @Override
    public synchronized void disconnect() throws Exception {
        if (!available) throw new IllegalStateException();
        if (file != null) file.unmap();
        available = false;
    }

    @Override
    public boolean sendMessage(byte[] bytes, int index, int length, MessageBlock target) {
        if (!available) throw new IllegalStateException();
        if (bytes.length < length || target.getPayloadSize() < length)
            throw new IllegalArgumentException("message too long");
        if (target.compareAndSwapInt(16, 0, 1)) {
            target.setBytes(64, bytes, index, length);
            return target.compareAndSwapInt(16, 1, 2);
        } else return false;
    }

    @Override
    public boolean readMessage(byte[] buf) {
        if (!available) throw new IllegalStateException();
        if (this.block.getInt(16) == 2) {
            this.block.getBytes(64, buf, 0, Math.min((int) this.block.getPayloadSize(), buf.length));
            return this.block.compareAndSwapInt(16, 2, 0);
        } else return false;
    }

    @Override
    public int getId() {
        if (!available) throw new IllegalStateException();
        return id;
    }

    private boolean login(long size, long blockSize, long timeout) {
        long offset = blockSize;
        while (offset < size) {
            int prevId = file.getInt(offset);
            long cur = System.currentTimeMillis();
            long prev = file.getAndSetLong(offset + 4, cur);
            if (Math.abs(prev - cur) > timeout) {
                if (file.compareAndSwapInt(offset, prevId, id)) {
                    this.block = new MessageBlock(file, offset, blockSize);
                    this.block.reset();
                    return true;
                }
            }
            offset += blockSize;
        }
        return false;
    }

    private static long round(long i) {
        return (i + 0xfffL) & ~0xfffL;
    }
}
