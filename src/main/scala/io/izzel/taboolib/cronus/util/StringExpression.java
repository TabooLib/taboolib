package io.izzel.taboolib.cronus.util;

import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.logger.TLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author 坏黑
 * @Since 2019-06-07 23:51
 */
public class StringExpression {

    @TInject
    private static TLogger logger;
    private static Pattern pattern = Pattern.compile("(?<symbol>>=|>|<=|<|==|=|!=|≈≈|≈|!≈)[ ]?(?<number>.+)");
    private String symbol;
    private StringNumber number;

    public StringExpression(Object in) {
        Matcher matcher = pattern.matcher(String.valueOf(in));
        if (!matcher.find()) {
            logger.error("StringExpression \"" + in + "\" parsing failed.");
            return;
        }
        symbol = matcher.group("symbol");
        number = new StringNumber(matcher.group("number"));
    }

    public boolean isSelect(String string) {
        switch (symbol) {
            case "=":
            case "==":
                return number.getSource().equals(string);
            case "!=":
                return !number.getSource().equals(string);
            case "≈":
            case "≈≈":
                return number.getSource().equalsIgnoreCase(string);
            case "!≈":
                return !number.getSource().equalsIgnoreCase(string);
            default:
                return false;
        }
    }

    public boolean isSelect(double number) {
        if (!this.number.isNumber()) {
            return false;
        }
        double v = this.number.getNumber().doubleValue();
        switch (symbol) {
            case ">":
                return number > v;
            case ">=":
                return number >= v;
            case "<":
                return number < v;
            case "<=":
                return number <= v;
            case "=":
            case "==":
                return number == v;
            default:
                return false;
        }
    }

    public String translate() {
        switch (symbol) {
            case ">":
                return TLocale.asString("translate-expression-0") + " " + number.getSource();
            case ">=":
                return TLocale.asString("translate-expression-1") + " " + number.getSource();
            case "<":
                return TLocale.asString("translate-expression-2") + " " + number.getSource();
            case "<=":
                return TLocale.asString("translate-expression-3") + " " + number.getSource();
            case "=":
            case "==":
                return TLocale.asString("translate-expression-4") + " " + number.getSource();
        }
        return symbol + " " + number.getSource();
    }

    @Override
    public String toString() {
        return "StringExpression{" +
                "symbol='" + symbol + '\'' +
                ", number=" + number +
                '}';
    }

    public String getSymbol() {
        return symbol;
    }

    public StringNumber getNumber() {
        return number;
    }
}
