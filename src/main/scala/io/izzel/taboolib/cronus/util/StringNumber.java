package io.izzel.taboolib.cronus.util;

import io.izzel.taboolib.cronus.CronusUtils;

/**
 * @author 坏黑
 * @since 2019-05-29 21:43
 */
public class StringNumber {

    private NumberType type;
    private Number number;
    private String source;

    public StringNumber(long number) {
        this.number = number;
        this.type = NumberType.INT;
    }

    public StringNumber(double number) {
        this.number = number;
        this.type = NumberType.DOUBLE;
    }

    public StringNumber(String source) {
        this.source = source;
        try {
            this.number = Double.parseDouble(this.source);
            this.type = CronusUtils.isInt(this.number.doubleValue()) ? NumberType.INT : NumberType.DOUBLE;
        } catch (Throwable ignored) {
            this.type = NumberType.STRING;
        }
    }

    public StringNumber add(String v) {
        StringNumber numberFormat = new StringNumber(v);
        if (isNumber() && numberFormat.isNumber()) {
            this.number = this.number.doubleValue() + numberFormat.getNumber().doubleValue();
            this.type = CronusUtils.isInt(this.number.doubleValue()) ? NumberType.INT : NumberType.DOUBLE;
        } else {
            this.source += numberFormat.getSource();
            this.type = NumberType.STRING;
        }
        return this;
    }

    public StringNumber subtract(String v) {
        StringNumber numberFormat = new StringNumber(v);
        if (isNumber() && numberFormat.isNumber()) {
            this.number = this.number.doubleValue() - numberFormat.getNumber().doubleValue();
            this.type = CronusUtils.isInt(this.number.doubleValue()) ? NumberType.INT : NumberType.DOUBLE;
        }
        return this;
    }

    public Object get() {
        switch (type) {
            case INT:
                return number.longValue();
            case DOUBLE:
                return number.doubleValue();
            default:
                return source;
        }
    }

    public boolean isNumber() {
        return type == NumberType.INT || type == NumberType.DOUBLE;
    }

    public Number getNumber() {
        return number;
    }

    public NumberType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public enum NumberType {

        DOUBLE, INT, STRING
    }

    @Override
    public String toString() {
        return "StringNumber{" +
                "type=" + type +
                ", number=" + number +
                ", source='" + source + '\'' +
                '}';
    }
}
