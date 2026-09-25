package taboolib.module.porticus.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通讯信息数据包读取工具
 *
 * @author 坏黑
 * @since 2020-10-15
 */
public class MessageReader {

    static final int MAX_PACKET_SIZE = 32767;
    static final int MAX_TOTAL = 1024;
    static final int MAX_CACHED_MESSAGES = 1024;
    static final long MAX_MESSAGE_SIZE = 4L * 1024 * 1024;
    static final long MAX_CACHED_BYTES = 16L * 1024 * 1024;
    static final long IDLE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(10);
    static final long COMPLETED_RETENTION_NANOS = TimeUnit.SECONDS.toNanos(10);
    static final long MAX_LIFETIME_NANOS = TimeUnit.SECONDS.toNanos(30);
    private static final long CLEANUP_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);

    private static final AtomicReference<CacheState> cache = new AtomicReference<>(new CacheState());

    /**
     * 清空消息缓存，并允许后续消息进入新的缓存状态。
     */
    public static void clear() {
        replaceCache(true);
    }

    /**
     * 打开消息接收缓存。
     */
    public static void open() {
        replaceCache(true);
    }

    /**
     * 关闭并清空消息接收缓存。
     */
    public static void close() {
        replaceCache(false);
    }

    /**
     * 清理过期的未完成消息和已消费消息。
     */
    public static void cleanUp() {
        cleanUp(cache.get(), System.nanoTime());
    }

    /**
     * 将通讯数据读取为数据包
     *
     * @param packet 通讯数据（未经过处理的原始内容）
     */
    public static Message read(byte[] packet) throws IOException {
        if (packet == null || packet.length == 0) {
            throw new ProtocolException("Message packet is empty");
        }
        if (packet.length > MAX_PACKET_SIZE) {
            throw new ProtocolException("Message packet exceeds protocol size limit");
        }
        try {
            String decoded = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(packet))
                    .toString();
            return readValidated(decoded, packet.length, System.nanoTime());
        } catch (CharacterCodingException ex) {
            throw new ProtocolException("Message packet is not valid UTF-8", ex);
        } catch (CacheCapacityException ex) {
            throw new CapacityException(ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ProtocolException("Invalid message packet", ex);
        }
    }

    /**
     * 通过通讯数据读取为数据包
     *
     * @param packet 通讯数据（未经过处理的原始内容）
     */
    public static Message read(String packet) {
        return read(packet, System.nanoTime());
    }

    static Message read(String packet, long now) {
        if (packet == null || packet.isEmpty()) {
            throw new IllegalArgumentException("Message packet is empty");
        }
        int packetBytes = packet.getBytes(StandardCharsets.UTF_8).length;
        if (packetBytes > MAX_PACKET_SIZE) {
            throw new IllegalArgumentException("Message packet exceeds protocol size limit");
        }
        return readValidated(packet, packetBytes, now);
    }

    private static Message readValidated(String source, int packetBytes, long now) {
        ParsedPacket packet = parse(source);
        while (true) {
            CacheState state = cache.get();
            if (state.closed) {
                throw new IllegalStateException("Message cache is closed");
            }
            cleanUpIfNeeded(state, now);
            String key = packet.uid.toString();
            Message message = computeMessage(state, key, packet, packetBytes, now, true);
            if (state.closed || cache.get() != state) {
                state.remove(key, message);
                continue;
            }
            if (message.isCompleted()) {
                try {
                    message.validatePayload();
                } catch (RuntimeException ex) {
                    state.remove(key, message);
                    throw ex;
                }
            }
            if (state.closed || cache.get() != state) {
                state.remove(key, message);
                continue;
            }
            return message;
        }
    }

    private static Message computeMessage(CacheState state, String key, ParsedPacket packet, int packetBytes, long now, boolean retryAfterCleanup) {
        AtomicReference<RuntimeException> deferredFailure = new AtomicReference<>();
        try {
            Message message = state.messages.compute(key, (ignored, current) -> {
                MessagePacket incoming = new MessagePacket(packet.uid, packet.data, packet.index, packet.total);
                if (current != null && current.isExpired(now)) {
                    state.release(current.releaseCachedBytes());
                    Message replacement = new Message(now);
                    try {
                        replacement.addPacket(incoming, packetBytes, now, state);
                        return replacement;
                    } catch (RuntimeException ex) {
                        state.slots.release();
                        deferredFailure.set(ex);
                        return null;
                    }
                }
                if (current != null) {
                    current.addPacket(incoming, packetBytes, now, state);
                    return current;
                }
                if (!state.slots.tryAcquire()) {
                    throw cacheCapacityExceeded("Message cache entry capacity exceeded");
                }
                Message created = new Message(now);
                try {
                    created.addPacket(incoming, packetBytes, now, state);
                    return created;
                } catch (RuntimeException ex) {
                    state.slots.release();
                    throw ex;
                }
            });
            RuntimeException failure = deferredFailure.get();
            if (failure != null) {
                throw failure;
            }
            return message;
        } catch (CacheCapacityException ex) {
            if (retryAfterCleanup && !state.closed) {
                cleanUp(state, now);
                return computeMessage(state, key, packet, packetBytes, now, false);
            }
            throw ex;
        }
    }

    static CacheCapacityException cacheCapacityExceeded(String message) {
        return new CacheCapacityException(message);
    }

    static void cleanUp(long now) {
        cleanUp(cache.get(), now);
    }

    static int cachedMessageCount() {
        return cache.get().messages.size();
    }

    static long cachedByteCount() {
        return cache.get().cachedBytes.get();
    }

    private static void cleanUpIfNeeded(CacheState state, long now) {
        long next = state.nextCleanup.get();
        if (now >= next && state.nextCleanup.compareAndSet(next, now + CLEANUP_INTERVAL_NANOS)) {
            cleanUp(state, now);
        }
    }

    private static void cleanUp(CacheState state, long now) {
        for (String key : state.messages.keySet()) {
            state.messages.computeIfPresent(key, (ignored, current) -> {
                if (current.isExpired(now)) {
                    state.release(current.releaseCachedBytes());
                    state.slots.release();
                    return null;
                }
                return current;
            });
        }
    }

    private static void replaceCache(boolean keepAccepting) {
        CacheState replacement = new CacheState(!keepAccepting);
        CacheState previous = cache.getAndSet(replacement);
        previous.closed = true;
        previous.clear();
    }

    private static ParsedPacket parse(String source) {
        JsonElement root;
        try {
            root = new JsonParser().parse(source);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Message packet is not valid JSON", ex);
        }
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Message packet must be a JSON object");
        }
        JsonObject json = root.getAsJsonObject();
        String uidSource = stringField(json, "uid");
        String data = stringField(json, "data");
        int index = integerField(json, "index");
        int total = integerField(json, "total");
        if (total < 1 || total > MAX_TOTAL) {
            throw new IllegalArgumentException("Message total is out of range");
        }
        if (index < 1 || index > total) {
            throw new IllegalArgumentException("Message index is out of range");
        }
        UUID uid;
        try {
            uid = UUID.fromString(uidSource);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Message UID is invalid", ex);
        }
        if (!uid.toString().equalsIgnoreCase(uidSource)) {
            throw new IllegalArgumentException("Message UID is invalid");
        }
        validateBase64Chunk(data, index, total);
        return new ParsedPacket(uid, data, index, total);
    }

    private static String stringField(JsonObject json, String name) {
        JsonElement element = json.get(name);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Message field '" + name + "' must be a string");
        }
        return element.getAsString();
    }

    private static int integerField(JsonObject json, String name) {
        JsonElement element = json.get(name);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Message field '" + name + "' must be an integer");
        }
        try {
            return new BigDecimal(element.getAsString()).intValueExact();
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalArgumentException("Message field '" + name + "' must be an integer", ex);
        }
    }

    private static void validateBase64Chunk(String data, int index, int total) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Message data is empty");
        }
        if (data.length() > MessageBuilder.MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message data exceeds packet chunk size limit");
        }
        boolean padding = false;
        int paddingLength = 0;
        for (int i = 0; i < data.length(); i++) {
            char character = data.charAt(i);
            if (character == '=') {
                if (index != total || ++paddingLength > 2) {
                    throw new IllegalArgumentException("Message data is not valid Base64");
                }
                padding = true;
            } else {
                boolean base64 = character >= 'A' && character <= 'Z'
                        || character >= 'a' && character <= 'z'
                        || character >= '0' && character <= '9'
                        || character == '+'
                        || character == '/';
                if (!base64 || padding) {
                    throw new IllegalArgumentException("Message data is not valid Base64");
                }
            }
        }
    }

    static final class CacheState {

        private final ConcurrentMap<String, Message> messages = new ConcurrentHashMap<>();
        private final Semaphore slots = new Semaphore(MAX_CACHED_MESSAGES);
        private final AtomicLong cachedBytes = new AtomicLong();
        private final AtomicLong nextCleanup = new AtomicLong();
        private volatile boolean closed;

        private CacheState() {
            this(false);
        }

        private CacheState(boolean closed) {
            this.closed = closed;
        }

        boolean reserve(long bytes) {
            while (true) {
                long current = cachedBytes.get();
                if (bytes < 0 || current > MAX_CACHED_BYTES - bytes) {
                    return false;
                }
                if (cachedBytes.compareAndSet(current, current + bytes)) {
                    return true;
                }
            }
        }

        void release(long bytes) {
            if (bytes != 0) {
                cachedBytes.addAndGet(-bytes);
            }
        }

        void remove(String key, Message message) {
            if (messages.remove(key, message)) {
                release(message.releaseCachedBytes());
                slots.release();
            }
        }

        void clear() {
            for (String key : messages.keySet()) {
                messages.computeIfPresent(key, (ignored, current) -> {
                    release(current.releaseCachedBytes());
                    slots.release();
                    return null;
                });
            }
        }
    }

    private static final class ParsedPacket {

        private final UUID uid;
        private final String data;
        private final int index;
        private final int total;

        private ParsedPacket(UUID uid, String data, int index, int total) {
            this.uid = uid;
            this.data = data;
            this.index = index;
            this.total = total;
        }
    }

    private static final class CacheCapacityException extends IllegalStateException {

        private CacheCapacityException(String message) {
            super(message);
        }
    }

    public static class ProtocolException extends IOException {

        public ProtocolException(String message) {
            super(message);
        }

        public ProtocolException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CapacityException extends IOException {

        public CapacityException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
