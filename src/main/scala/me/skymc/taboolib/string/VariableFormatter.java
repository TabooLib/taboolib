package me.skymc.taboolib.string;

import com.ilummc.tlib.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author sky
 * @Since 2018-05-27 11:33
 */
public class VariableFormatter {

    private Pattern pattern;
    private String text;
    private String textOrigin;
    private List<Variable> variableList = new ArrayList<>();

    public VariableFormatter(String text) {
        this(text, "<([^<>]+)>");
    }

    public VariableFormatter(String text, String regex) {
        this(text, Pattern.compile(regex));
    }

    public VariableFormatter(String text, Pattern pattern) {
        this.text = text;
        this.textOrigin = text;
        this.pattern = pattern;
    }

    public VariableFormatter reset() {
        text = textOrigin;
        variableList.clear();
        return this;
    }

    public VariableFormatter find() {
        reset();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String group = matcher.group();
            String[] textOther = text.split(group);
            String textLeft = text.indexOf(group) > 0 ? textOther[0] : null;
            String textRight = textOther.length >= 2 ? text.substring(text.indexOf(group) + group.length()) : null;
            if (textLeft != null) {
                variableList.add(new Variable(textLeft, false));
            }
            variableList.add(new Variable(group.substring(1, group.length() - 1), true));
            if (textRight != null && !pattern.matcher(textRight).find()) {
                variableList.add(new Variable(textRight, false));
            } else {
                text = String.valueOf(textRight);
            }
        }
        if (variableList.size() == 0) {
            variableList.add(new Variable(text, false));
        }
        return this;
    }

    @Override
    public String toString() {
        return Strings.replaceWithOrder("VariableFormatter'{'pattern={0}, text=''{1}'', textOrigin=''{2}'', variableList={3}'}'", pattern, text, textOrigin, variableList);
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public String getText() {
        return text;
    }

    public List<Variable> getVariableList() {
        return variableList;
    }

    // *********************************
    //
    //         Public classes
    //
    // *********************************

    public static class Variable {

        private final String text;
        private final boolean variable;

        public Variable(String text, boolean variable) {
            this.text = text;
            this.variable = variable;
        }

        public String getText() {
            return text;
        }

        public boolean isVariable() {
            return variable;
        }

        @Override
        public String toString() {
            return Strings.replaceWithOrder("Variable'{'text=''{0}'', variable={1}'}'", text, variable);
        }
    }
}
