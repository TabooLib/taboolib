package io.izzel.taboolib.module.nms.nbt;

import io.izzel.taboolib.util.KV;
import org.bukkit.util.NumberConversions;

import java.util.Arrays;

/**
 * @Author sky
 * @Since 2019-10-22 12:06
 */
public enum NBTOperation {

    ADD_NUMBER, ADD_SCALAR, MULTIPLY_SCALAR_1;

    public static NBTOperation fromIndex(int index) {
        return Arrays.stream(values()).filter(operation -> operation.ordinal() == index).findFirst().orElse(ADD_NUMBER);
    }

    public static KV<Double, NBTOperation> fromSimple(String in) {
        if (in.endsWith("%")) {
            return new KV(NumberConversions.toDouble(in.substring(0, in.length() - 1)), ADD_SCALAR);
        } else {
            return new KV(NumberConversions.toDouble(in), ADD_NUMBER);
        }
    }
}
