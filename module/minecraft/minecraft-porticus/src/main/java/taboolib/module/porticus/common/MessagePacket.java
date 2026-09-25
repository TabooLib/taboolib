package taboolib.module.porticus.common;

import java.util.UUID;

/**
 * 通讯信息数据包
 * 因客户端限制每个数据包大小不超过 32767 字节
 * 故需要将较大数据包分割为数个小数据包分别发送
 * <p>
 * 数据包格式如下
 * {
 * "uid": "0000-0000-0000-0000",
 * "data": "abc",
 * "index": 1,
 * "total": 100
 * }
 *
 * @author 坏黑
 * @since 2019-02-13 9:28
 */
public class MessagePacket {

    private final UUID uid;
    private final String data;
    private final int index;
    private final int total;

    MessagePacket(UUID uid, String data, int index, int total) {
        if (uid == null) {
            throw new IllegalArgumentException("Message UID is required");
        }
        if (data == null) {
            throw new IllegalArgumentException("Message data is required");
        }
        if (total < 1 || total > MessageReader.MAX_TOTAL) {
            throw new IllegalArgumentException("Message total is out of range");
        }
        if (index < 1 || index > total) {
            throw new IllegalArgumentException("Message index is out of range");
        }
        this.uid = uid;
        this.data = data;
        this.index = index;
        this.total = total;
    }

    public UUID getUID() {
        return uid;
    }

    public String getData() {
        return data;
    }

    public int getIndex() {
        return index;
    }

    public int getTotal() {
        return total;
    }
}
