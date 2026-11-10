package taboolib.module.porticus.common;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 通讯信息容器
 *
 * @author 坏黑
 * @since 2019-02-13 11:07
 */
public class Message {

    private final List<MessagePacket> messages = Lists.newCopyOnWriteArrayList();
    private final List<MessagePacket> exposedMessages = new AbstractList<MessagePacket>() {
        @Override
        public MessagePacket get(int index) {
            return messages.get(index);
        }

        @Override
        public int size() {
            return messages.size();
        }

        @Override
        public MessagePacket set(int index, MessagePacket element) {
            synchronized (Message.this) {
                MessagePacket previous = messages.set(index, element);
                invalidateDecodedArguments();
                return previous;
            }
        }

        @Override
        public void add(int index, MessagePacket element) {
            synchronized (Message.this) {
                messages.add(index, element);
                invalidateDecodedArguments();
            }
        }

        @Override
        public MessagePacket remove(int index) {
            synchronized (Message.this) {
                MessagePacket removed = messages.remove(index);
                invalidateDecodedArguments();
                return removed;
            }
        }

        @Override
        public void clear() {
            synchronized (Message.this) {
                if (!messages.isEmpty()) {
                    messages.clear();
                    invalidateDecodedArguments();
                }
            }
        }
    };
    private final AtomicBoolean built = new AtomicBoolean();
    private final long createdAt;
    private volatile long lastAccess;
    private volatile long completedAt;
    private volatile String[] decodedArguments;
    private long cachedBytes;

    public Message() {
        this(System.nanoTime());
    }

    Message(long createdAt) {
        this.createdAt = createdAt;
        this.lastAccess = createdAt;
    }

    /**
     * 构建为可读取的通讯内容
     */
    @NotNull
    public String[] build() {
        String[] arguments = decodedArguments;
        if (arguments == null) {
            synchronized (this) {
                arguments = decodedArguments;
                if (arguments == null) {
                    arguments = decodeArguments();
                    decodedArguments = arguments;
                }
            }
        }
        return arguments.clone();
    }

    /**
     * 在所有数据包接收完成后仅构建一次。
     *
     * @return 首次完整构建的内容，尚未完成或已经构建时返回 null
     */
    @Nullable
    public String[] buildOnce() {
        if (!isCompleted() || !built.compareAndSet(false, true)) {
            return null;
        }
        try {
            return build();
        } catch (RuntimeException ex) {
            built.set(false);
            throw ex;
        }
    }

    /**
     * 所有数据包是否接收完成
     */
    public boolean isCompleted() {
        List<MessagePacket> snapshot = Lists.newArrayList(messages);
        if (snapshot.isEmpty()) {
            return false;
        }
        try {
            validateCompleted(snapshot);
            return true;
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    /**
     * 获取消息 UID。
     *
     * @return 尚未接收任何数据包时返回 null
     */
    @Nullable
    public UUID getUID() {
        return messages.isEmpty() ? null : messages.get(0).getUID();
    }

    /**
     * 获取实时数据包列表，保持旧版 API 的可修改语义。
     */
    @NotNull
    public List<MessagePacket> getMessages() {
        return exposedMessages;
    }

    synchronized boolean addPacket(MessagePacket packet, int packetBytes, long now, MessageReader.CacheState cache) {
        for (MessagePacket message : messages) {
            if (!message.getUID().equals(packet.getUID())) {
                throw new IllegalArgumentException("Message UID is inconsistent");
            }
            if (message.getTotal() != packet.getTotal()) {
                throw new IllegalArgumentException("Message total is inconsistent");
            }
            if (message.getIndex() == packet.getIndex()) {
                if (message.getData().equals(packet.getData())) {
                    lastAccess = now;
                    return false;
                }
                throw new IllegalArgumentException("Message packet data conflicts with an existing index");
            }
        }
        if (cachedBytes + packetBytes > MessageReader.MAX_MESSAGE_SIZE) {
            throw new IllegalArgumentException("Message exceeds protocol cache size limit");
        }
        if (!cache.reserve(packetBytes)) {
            throw MessageReader.cacheCapacityExceeded("Message cache byte capacity exceeded");
        }
        boolean added = false;
        try {
            messages.add(packet);
            cachedBytes += packetBytes;
            lastAccess = now;
            decodedArguments = null;
            if (messages.size() == packet.getTotal()) {
                completedAt = now;
            }
            added = true;
            return true;
        } finally {
            if (!added) {
                cache.release(packetBytes);
            }
        }
    }

    void validatePayload() {
        if (decodedArguments == null) {
            build();
        }
    }

    synchronized long releaseCachedBytes() {
        long released = cachedBytes;
        cachedBytes = 0;
        return released;
    }

    boolean isExpired(long now) {
        long completed = completedAt;
        return now - lastAccess >= MessageReader.IDLE_TIMEOUT_NANOS
                || now - createdAt >= MessageReader.MAX_LIFETIME_NANOS
                || completed != 0 && now - completed >= MessageReader.COMPLETED_RETENTION_NANOS;
    }

    private String[] decodeArguments() {
        List<MessagePacket> snapshot = Lists.newArrayList(messages);
        validateCompleted(snapshot);
        messages.sort(Comparator.comparingInt(MessagePacket::getIndex));
        snapshot = Lists.newArrayList(messages);
        StringBuilder builder = new StringBuilder();
        for (MessagePacket message : snapshot) {
            builder.append(message.getData());
        }
        JsonElement element;
        try {
            element = new JsonParser().parse(ByteUtils.deSerialize(builder.toString()));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Message payload is not valid JSON", ex);
        }
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Message payload must be a JSON array");
        }
        JsonArray json = element.getAsJsonArray();
        String[] args = new String[json.size()];
        for (int i = 0; i < json.size(); i++) {
            JsonElement argument = json.get(i);
            if (!argument.isJsonPrimitive() || !argument.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Message argument must be a string");
            }
            args[i] = argument.getAsString();
        }
        return args;
    }

    private void invalidateDecodedArguments() {
        decodedArguments = null;
        completedAt = 0;
    }

    private static void validateCompleted(List<MessagePacket> packets) {
        if (packets.isEmpty()) {
            throw new IllegalStateException("Message is incomplete");
        }
        MessagePacket first = packets.get(0);
        int total = first.getTotal();
        if (packets.size() != total) {
            throw new IllegalStateException("Message is incomplete");
        }
        Set<Integer> indexes = new HashSet<>();
        for (MessagePacket packet : packets) {
            if (!first.getUID().equals(packet.getUID()) || packet.getTotal() != total) {
                throw new IllegalStateException("Message metadata is inconsistent");
            }
            if (packet.getIndex() < 1 || packet.getIndex() > total || !indexes.add(packet.getIndex())) {
                throw new IllegalStateException("Message indexes are invalid");
            }
        }
    }
}
