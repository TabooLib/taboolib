package taboolib.module.porticus.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 通讯信息数据包读取工具
 *
 * @author 坏黑
 * @since 2020-10-15
 */
public class MessageReader {

    private static final Cache<String, Message> queueMessages = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    /**
     * 将通讯数据读取为数据包
     *
     * @param packet 通讯数据（未经过处理的原始内容）
     */
    public static Message read(byte[] packet) throws IOException {
        return read(new String(packet, StandardCharsets.UTF_8));
    }

    /**
     * 通过通讯数据读取为数据包
     *
     * @param packet 通讯数据（未经过处理的原始内容）
     */
    public static Message read(String packet) {
        JsonObject json = new JsonParser().parse(packet).getAsJsonObject();
        Message message = queueMessages.getIfPresent(json.get("uid").getAsString());
        if (message == null) {
            message = new Message();
            queueMessages.put(json.get("uid").getAsString(), message);
        }
        message.getMessages().add(new MessagePacket(
                UUID.fromString(json.get("uid").getAsString()),
                json.get("data").getAsString(),
                json.get("index").getAsInt(),
                json.get("total").getAsInt()
        ));
        return message;
    }
}
