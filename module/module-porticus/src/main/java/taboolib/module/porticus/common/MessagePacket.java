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
