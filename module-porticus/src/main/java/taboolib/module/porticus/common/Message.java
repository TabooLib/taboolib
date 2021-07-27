package taboolib.module.porticus.common;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/**
 * 通讯信息容器
 *
 * @author 坏黑
 * @since 2019-02-13 11:07
 */
public class Message {

    private final List<MessagePacket> messages = Lists.newCopyOnWriteArrayList();

    /**
     * 构建为可读取的通讯内容
     */
    @NotNull
    public String[] build() {
        StringBuilder builder = new StringBuilder();
        messages.sort(Comparator.comparingInt(MessagePacket::getIndex));
        messages.forEach(message -> {
            builder.append(message.getData());
        });
        JsonArray json = new JsonParser().parse(ByteUtils.deSerialize(builder.toString())).getAsJsonArray();
        String[] args = new String[json.size()];
        for (int i = 0; i < json.size(); i++) {
            args[i] = json.get(i).getAsString();
        }
        return args;
    }

    /**
     * 所有数据包是否接收完成
     */
    public boolean isCompleted() {
        return !messages.isEmpty() && messages.size() == messages.get(0).getTotal();
    }

    @NotNull
    public List<MessagePacket> getMessages() {
        return messages;
    }
}
