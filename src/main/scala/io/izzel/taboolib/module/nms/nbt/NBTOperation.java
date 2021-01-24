package io.izzel.taboolib.module.nms.nbt;

import io.izzel.taboolib.util.Pair;
import org.bukkit.util.NumberConversions;

import java.util.Arrays;

/**
 * 物品 NBT 结构映射类
 *
 * @author sky
 * @since 2019-10-22 12:06
 */
public enum NBTOperation {

    ADD_NUMBER, ADD_SCALAR, MULTIPLY_SCALAR_1;

    public static NBTOperation fromIndex(int index) {
        return Arrays.stream(values()).filter(operation -> operation.ordinal() == index).findFirst().orElse(ADD_NUMBER);
    }

    public static Pair<Double, NBTOperation> fromSimple(String in) {
        if (in.endsWith("%")) {
            return new Pair<>(NumberConversions.toDouble(in.substring(0, in.length() - 1)), ADD_SCALAR);
        } else {
            return new Pair<>(NumberConversions.toDouble(in), ADD_NUMBER);
        }
    }
}
