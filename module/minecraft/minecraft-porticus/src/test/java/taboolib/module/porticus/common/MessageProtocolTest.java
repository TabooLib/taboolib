package taboolib.module.porticus.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageProtocolTest {

    @BeforeEach
    void resetCache() {
        MessageReader.clear();
    }

    @AfterEach
    void verifyCacheCanBeCleared() {
        MessageReader.clear();
        assertEquals(0, MessageReader.cachedMessageCount());
        assertEquals(0, MessageReader.cachedByteCount());
    }

    @Test
    void shouldRoundTripNormalMessage() throws IOException {
        String uid = UUID.randomUUID().toString();
        String[] source = {uid, "command", "first", "second"};

        Message message = readAll(MessageBuilder.create(source));

        assertTrue(message.isCompleted());
        assertEquals(UUID.fromString(uid), message.getUID());
        assertArrayEquals(new String[]{"command", "first", "second"}, message.build());
    }

    @Test
    void shouldRoundTripUnicodeMessage() throws IOException {
        String[] source = {UUID.randomUUID().toString(), "你好，世界", "emoji: 😀", "日本語", "Привет"};

        Message message = readAll(MessageBuilder.create(source));

        assertArrayEquals(new String[]{"你好，世界", "emoji: 😀", "日本語", "Привет"}, message.build());
    }

    @Test
    void shouldSplitAndReassembleMultiplePackets() throws IOException {
        String large = repeat('a', MessageBuilder.MESSAGE_LENGTH * 2);
        String[] source = {UUID.randomUUID().toString(), large, "tail"};
        List<byte[]> packets = MessageBuilder.create(source);

        assertTrue(packets.size() > 1);
        for (byte[] packet : packets) {
            assertTrue(packet.length <= MessageReader.MAX_PACKET_SIZE);
        }
        Message message = readAll(packets);
        assertArrayEquals(new String[]{large, "tail"}, message.build());
    }

    @Test
    void shouldReassembleOutOfOrderPackets() throws IOException {
        String large = repeat('b', MessageBuilder.MESSAGE_LENGTH * 2);
        List<byte[]> packets = new ArrayList<>(MessageBuilder.create(new String[]{UUID.randomUUID().toString(), large}));
        Collections.reverse(packets);

        Message message = readAll(packets);

        assertTrue(message.isCompleted());
        assertArrayEquals(new String[]{large}, message.build());
        for (int i = 0; i < message.getMessages().size(); i++) {
            assertEquals(i + 1, message.getMessages().get(i).getIndex());
        }
    }

    @Test
    void shouldDeduplicatePacketsByIndex() throws IOException {
        String large = repeat('c', MessageBuilder.MESSAGE_LENGTH * 2);
        List<byte[]> packets = MessageBuilder.create(new String[]{UUID.randomUUID().toString(), large});

        Message message = MessageReader.read(packets.get(0));
        Message duplicate = MessageReader.read(packets.get(0));

        assertEquals(1, duplicate.getMessages().size());
        assertEquals(message, duplicate);
        for (int i = 1; i < packets.size(); i++) {
            message = MessageReader.read(packets.get(i));
        }
        assertTrue(message.isCompleted());
        assertArrayEquals(new String[]{large}, message.build());
    }

    @Test
    void shouldRejectConflictingDataForTheSameIndex() throws IOException {
        List<byte[]> packets = MessageBuilder.create(new String[]{UUID.randomUUID().toString(), repeat('c', MessageBuilder.MESSAGE_LENGTH * 2)});
        Message message = MessageReader.read(packets.get(0));
        JsonObject conflict = new JsonParser().parse(new String(packets.get(0), StandardCharsets.UTF_8)).getAsJsonObject();
        String data = conflict.get("data").getAsString();
        conflict.addProperty("data", (data.charAt(0) == 'A' ? 'B' : 'A') + data.substring(1));

        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(conflict.toString()));
        assertEquals(1, message.getMessages().size());
    }

    @Test
    void shouldRemainIncompleteWhenPacketIsMissing() throws IOException {
        String large = repeat('d', MessageBuilder.MESSAGE_LENGTH * 2);
        List<byte[]> packets = MessageBuilder.create(new String[]{UUID.randomUUID().toString(), large});
        Message message = null;

        for (int i = 0; i < packets.size() - 1; i++) {
            message = MessageReader.read(packets.get(i));
        }

        assertFalse(message.isCompleted());
        assertNull(message.buildOnce());
        Message incomplete = message;
        assertThrows(IllegalStateException.class, incomplete::build);
    }

    @Test
    void shouldRejectIndexesAndTotalsOutsideProtocolBounds() {
        String uid = UUID.randomUUID().toString();
        String data = ByteUtils.serialize("[]");

        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(packet(uid, data, 0, 1)));
        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(packet(uid, data, 2, 1)));
        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(packet(uid, data, 1, 0)));
        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(packet(uid, data, 1, MessageReader.MAX_TOTAL + 1)));
    }

    @Test
    void shouldRejectConflictingTotalWithoutMutatingCachedMessage() throws IOException {
        String large = repeat('e', MessageBuilder.MESSAGE_LENGTH * 2);
        List<byte[]> packets = MessageBuilder.create(new String[]{UUID.randomUUID().toString(), large});
        Message message = MessageReader.read(packets.get(0));
        JsonObject conflict = new JsonParser().parse(new String(packets.get(0), StandardCharsets.UTF_8)).getAsJsonObject();
        conflict.addProperty("total", conflict.get("total").getAsInt() + 1);

        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(conflict.toString()));
        assertEquals(1, message.getMessages().size());
        for (int i = 1; i < packets.size(); i++) {
            MessageReader.read(packets.get(i));
        }
        assertTrue(message.isCompleted());
        assertArrayEquals(new String[]{large}, message.build());
    }

    @Test
    void shouldRejectMalformedJsonBase64AndFieldTypesWithoutPollutingCache() throws IOException {
        String uid = UUID.randomUUID().toString();

        assertThrows(IllegalArgumentException.class, () -> MessageReader.read("not-json"));
        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(packet(uid, "%%%", 1, 1)));
        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(packet("1-1-1-1-1", ByteUtils.serialize("[]"), 1, 1)));

        JsonObject wrongType = new JsonObject();
        wrongType.addProperty("uid", uid);
        wrongType.addProperty("data", ByteUtils.serialize("[]"));
        wrongType.addProperty("index", "1");
        wrongType.addProperty("total", 1);
        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(wrongType.toString()));

        Message valid = readAll(MessageBuilder.create(new String[]{uid, "valid"}));
        assertTrue(valid.isCompleted());
        assertArrayEquals(new String[]{"valid"}, valid.build());
    }

    @Test
    void shouldRejectInvalidOuterAndInnerUtf8() throws IOException {
        assertThrows(IOException.class, () -> MessageReader.read(new byte[]{(byte) 0xC3, 0x28}));

        String uid = UUID.randomUUID().toString();
        byte[] invalidJsonBytes = new byte[]{'[', '"', (byte) 0xC3, '"', ']'};
        String encoded = Base64.getEncoder().encodeToString(invalidJsonBytes);
        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(packet(uid, encoded, 1, 1)));
        assertEquals(0, MessageReader.cachedMessageCount());

        Message valid = readAll(MessageBuilder.create(new String[]{uid, "valid"}));
        assertArrayEquals(new String[]{"valid"}, valid.build());
    }

    @Test
    void shouldRejectOversizedRawPacketAndMessage() {
        String oversized = repeat('x', MessageReader.MAX_PACKET_SIZE + 1);

        assertThrows(IllegalArgumentException.class, () -> MessageReader.read(oversized));
        assertThrows(IOException.class, () -> MessageReader.read(oversized.getBytes(StandardCharsets.UTF_8)));
        assertThrows(IOException.class, () -> MessageBuilder.create(new String[]{
                UUID.randomUUID().toString(),
                repeat('x', (int) MessageReader.MAX_MESSAGE_SIZE)
        }));
    }

    @Test
    void shouldBuildCompletedMessageOnlyOnce() throws IOException {
        String large = repeat('f', MessageBuilder.MESSAGE_LENGTH * 2);
        Message message = readAll(MessageBuilder.create(new String[]{UUID.randomUUID().toString(), large, "done"}));

        assertArrayEquals(new String[]{large, "done"}, message.buildOnce());
        assertNull(message.buildOnce());
        assertTrue(message.isCompleted());
        assertArrayEquals(new String[]{large, "done"}, message.build());
    }

    @Test
    void shouldSuppressCompletedMessageReplayUntilRetentionExpires() throws IOException {
        List<byte[]> packets = MessageBuilder.create(new String[]{UUID.randomUUID().toString(), repeat('g', MessageBuilder.MESSAGE_LENGTH * 2)});
        long now = 1_000;
        Message completed = readAll(packets, now);
        assertArrayEquals(new String[]{repeat('g', MessageBuilder.MESSAGE_LENGTH * 2)}, completed.buildOnce());

        Message replay = readAll(packets, now + 1);

        assertSame(completed, replay);
        assertNull(replay.buildOnce());
        MessageReader.cleanUp(now + MessageReader.COMPLETED_RETENTION_NANOS + 1);
        Message next = MessageReader.read(new String(packets.get(0), StandardCharsets.UTF_8), now + MessageReader.COMPLETED_RETENTION_NANOS + 2);
        assertNotSame(completed, next);
        assertFalse(next.isCompleted());
    }

    @Test
    void shouldExpireIdlePartialMessageWithoutWaiting() throws IOException {
        List<byte[]> packets = MessageBuilder.create(new String[]{UUID.randomUUID().toString(), repeat('h', MessageBuilder.MESSAGE_LENGTH * 2)});
        long now = 10_000;
        Message partial = MessageReader.read(new String(packets.get(0), StandardCharsets.UTF_8), now);

        MessageReader.cleanUp(now + MessageReader.IDLE_TIMEOUT_NANOS + 1);

        assertEquals(0, MessageReader.cachedMessageCount());
        assertEquals(0, MessageReader.cachedByteCount());
        Message replacement = MessageReader.read(new String(packets.get(0), StandardCharsets.UTF_8), now + MessageReader.IDLE_TIMEOUT_NANOS + 2);
        assertNotSame(partial, replacement);
    }

    @Test
    void shouldEnforcePerMessageAndEntryCacheCapacity() {
        String uid = UUID.randomUUID().toString();
        String chunk = repeat('A', MessageBuilder.MESSAGE_LENGTH);
        boolean rejected = false;
        for (int index = 1; index <= 200; index++) {
            try {
                MessageReader.read(packet(uid, chunk, index, 200));
            } catch (IllegalArgumentException ex) {
                rejected = true;
                break;
            }
        }
        assertTrue(rejected);
        assertTrue(MessageReader.cachedByteCount() <= MessageReader.MAX_MESSAGE_SIZE);

        MessageReader.clear();
        String partialData = "Ww";
        for (int i = 0; i < MessageReader.MAX_CACHED_MESSAGES; i++) {
            MessageReader.read(packet(UUID.randomUUID().toString(), partialData, 1, 2));
        }
        assertEquals(MessageReader.MAX_CACHED_MESSAGES, MessageReader.cachedMessageCount());
        assertThrows(IllegalStateException.class, () -> MessageReader.read(packet(UUID.randomUUID().toString(), partialData, 1, 2)));
    }

    @Test
    void shouldRejectNewPacketsWhileCacheIsClosed() throws IOException {
        String packet = new String(MessageBuilder.create(new String[]{UUID.randomUUID().toString(), "value"}).get(0), StandardCharsets.UTF_8);

        MessageReader.close();
        assertThrows(IllegalStateException.class, () -> MessageReader.read(packet));
        assertEquals(0, MessageReader.cachedMessageCount());

        MessageReader.open();
        assertTrue(MessageReader.read(packet).isCompleted());
    }

    @Test
    void shouldPreserveMutableLivePacketListCompatibility() throws IOException {
        Message message = MessageReader.read(MessageBuilder.create(new String[]{UUID.randomUUID().toString(), "value"}).get(0));
        assertArrayEquals(new String[]{"value"}, message.build());

        message.getMessages().clear();

        assertFalse(message.isCompleted());
        assertThrows(IllegalStateException.class, message::build);
    }

    private static Message readAll(List<byte[]> packets) throws IOException {
        Message message = null;
        for (byte[] packet : packets) {
            message = MessageReader.read(packet);
        }
        return message;
    }

    private static Message readAll(List<byte[]> packets, long now) {
        Message message = null;
        for (byte[] packet : packets) {
            message = MessageReader.read(new String(packet, StandardCharsets.UTF_8), now);
        }
        return message;
    }

    private static String packet(String uid, String data, int index, int total) {
        JsonObject json = new JsonObject();
        json.addProperty("uid", uid);
        json.addProperty("data", data);
        json.addProperty("index", index);
        json.addProperty("total", total);
        return json.toString();
    }

    private static String repeat(char character, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(character);
        }
        return builder.toString();
    }
}
