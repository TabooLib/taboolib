package taboolib.module.nms;

import com.google.common.base.Enums;

/**
 * 物品 NBT 类型映射
 *
 * @author 坏黑
 * @since 2019-05-24 17:46
 */
public enum ItemTagType {

    END(0),

    BYTE(1),

    SHORT(2),

    INT(3),

    LONG(4),

    FLOAT(5),

    DOUBLE(6),

    BYTE_ARRAY(7),

    INT_ARRAY(11),

    STRING(8),

    LIST(9),

    COMPOUND(10);

    private final int id;

    ItemTagType(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public static ItemTagType parse(String in) {
        return Enums.getIfPresent(ItemTagType.class, in).or(END);
    }
}
