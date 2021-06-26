package taboolib.module.porticus.common;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        List<byte[]> messages = Lists.newArrayList();
        JsonArray array = new JsonArray();
        for (int i = 1; i < message.length; i++) {
            array.add(new JsonPrimitive(message[i]));
        }
        String source = ByteUtils.serialize(array.toString());
        int times = (int) Math.ceil(source.length() / (double) MESSAGE_LENGTH);
        for (int i = 0; i < times; i++) {
            JsonObject json = new JsonObject();
            json.addProperty("uid", message[0]);
            json.addProperty("index", i + 1);
            json.addProperty("total", times);
            if (source.length() < MESSAGE_LENGTH) {
                json.addProperty("data", source);
            } else {
                json.addProperty("data", source.substring(0, source.length() - (source.length() - MESSAGE_LENGTH)));
                source = source.substring(MESSAGE_LENGTH);
            }
            messages.add(json.toString().getBytes(StandardCharsets.UTF_8));
        }
        return messages;
    }
}
