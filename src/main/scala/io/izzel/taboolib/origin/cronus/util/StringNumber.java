package io.izzel.taboolib.origin.cronus.util;

/**
 * @Author 坏黑
 * @Since 2019-05-29 21:43
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
            number = Long.valueOf(source);
            type = NumberType.INT;
        } catch (Throwable ignored) {
            try {
                number = Double.valueOf(source);
                type = NumberType.DOUBLE;
            } catch (Throwable ignored2) {
                type = NumberType.STRING;
            }
        }
    }

    public StringNumber add(String v) {
        StringNumber numberFormat = new StringNumber(v);
        if (isNumber() && numberFormat.isNumber()) {
            if (type == NumberType.INT && numberFormat.getType() == NumberType.INT) {
                number = number.longValue() + numberFormat.getNumber().longValue();
            } else {
                number = number.doubleValue() + numberFormat.getNumber().doubleValue();
                type = NumberType.DOUBLE;
            }
        } else {
            source += numberFormat.getSource();
            type = NumberType.STRING;
        }
        return this;
    }

    public StringNumber subtract(String v) {
        StringNumber numberFormat = new StringNumber(v);
        if (isNumber() && numberFormat.isNumber()) {
            if (type == NumberType.INT && numberFormat.getType() == NumberType.INT) {
                number = number.longValue() - numberFormat.getNumber().longValue();
            } else {
                number = number.doubleValue() - numberFormat.getNumber().doubleValue();
                type = NumberType.DOUBLE;
            }
        }
        return this;
    }

    public StringNumber multiply(String v) {
        StringNumber numberFormat = new StringNumber(v);
        if (isNumber() && numberFormat.isNumber()) {
            if (type == NumberType.INT && numberFormat.getType() == NumberType.INT) {
                number = number.longValue() * numberFormat.getNumber().longValue();
            } else {
                number = number.doubleValue() * numberFormat.getNumber().doubleValue();
                type = NumberType.DOUBLE;
            }
        }
        return this;
    }

    public StringNumber division(String v) {
        StringNumber numberFormat = new StringNumber(v);
        if (isNumber() && numberFormat.isNumber()) {
            if (type == NumberType.INT && numberFormat.getType() == NumberType.INT) {
                number = number.longValue() / numberFormat.getNumber().longValue();
            } else {
                number = number.doubleValue() / numberFormat.getNumber().doubleValue();
                type = NumberType.DOUBLE;
            }

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
