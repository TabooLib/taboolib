package taboolib.module.porticus.common;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 通讯信息数据包创建工具
 *
 * @author 坏黑
 * @since 2020-10-15
 */
public class MessageBuilder {

    /**
     * 单个数据包允许的最大字节
     */
    public static final int MESSAGE_LENGTH = 30000;

    /**
     * 将源数据分割为数个大小合理且协议相同的数据包
     * 第一个参数将作为数据包的 UID 识别
     *
     * @param message 源数据
     */
    public static List<byte[]> create(String[] message) throws IOException {
        if (message == null || message.length == 0 || message[0] == null) {
            throw new IOException("Message UID is required");
        }
        UUID uid;
        try {
            uid = UUID.fromString(message[0]);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Message UID is invalid", ex);
        }
        if (!uid.toString().equalsIgnoreCase(message[0])) {
            throw new IOException("Message UID is invalid");
        }
        List<byte[]> messages = Lists.newArrayList();
        JsonArray array = new JsonArray();
        for (int i = 1; i < message.length; i++) {
            if (message[i] == null) {
                throw new IOException("Message arguments cannot be null");
            }
            array.add(new JsonPrimitive(message[i]));
        }
        String source = ByteUtils.serialize(array.toString());
        int times = (source.length() + MESSAGE_LENGTH - 1) / MESSAGE_LENGTH;
        if (times < 1 || times > MessageReader.MAX_TOTAL) {
            throw new IOException("Message contains too many packets");
        }
        long totalBytes = 0;
        for (int i = 0; i < times; i++) {
            int from = i * MESSAGE_LENGTH;
            int to = Math.min(from + MESSAGE_LENGTH, source.length());
            JsonObject json = new JsonObject();
            json.addProperty("uid", uid.toString());
            json.addProperty("index", i + 1);
            json.addProperty("total", times);
            json.addProperty("data", source.substring(from, to));
            byte[] packet = json.toString().getBytes(StandardCharsets.UTF_8);
            if (packet.length > MessageReader.MAX_PACKET_SIZE) {
                throw new IOException("Message packet exceeds protocol size limit");
            }
            totalBytes += packet.length;
            if (totalBytes > MessageReader.MAX_MESSAGE_SIZE) {
                throw new IOException("Message exceeds protocol cache size limit");
            }
            messages.add(packet);
        }
        return messages;
    }
}
