package taboolib.common.env;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for any class that needs to do XML parsing
 *
 * @author Zach Deibert, sky
 * @since 1.0.0
 */
public abstract class AbstractXmlParser {

    /**
     * The pattern to use to detect when a variable should be substituted in the
     * pom
     *
     * @since 1.0.0
     */
    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Gets the replacement value for a substitution variable
     *
     * @param key The key of the variable
     * @param pom The pom document
     * @return The value that it should be replaced with
     * @throws ParseException If the variable could not be resolved
     * @since 1.0.0
     */
    @NotNull
    private static String getReplacement(String key, Element pom) throws ParseException {
        if (key.startsWith("project.")) {
            return find(key.substring("project.".length()), pom);
        } else if (key.startsWith("pom.")) {
            return find(key.substring("pom.".length()), pom);
        } else {
            throw new ParseException(String.format("Unknown variable '%s'", key), -1);
        }
    }

    /**
     * Replaces all the variables in a string of text
     *
     * @param text The text to replace the variables in
     * @param pom  The pom document
     * @return The text with all the variables replaced
     * @throws ParseException If the variable could not be resolved
     * @since 1.0.0
     */
    @NotNull
    private static String replaceVariables(String text, Element pom) throws ParseException {
        Matcher matcher = SUBSTITUTION_PATTERN.matcher(text);
        while (matcher.find()) {
            text = matcher.replaceFirst(getReplacement(matcher.group(1), pom));
        }
        return text;
    }


    @NotNull
    protected static String find(String name, Element node) throws ParseException {
        return find(name, node, null);
    }


    /**
     * Searches for a node and returns the text inside of it
     *
     * @param name The name of the node to search for
     * @param node The node to search inside of
     * @param def  The default value, or <code>null</code> if the value is
     *             required
     * @return The text content of the node it found, or <code>def</code> if the
     * node is not found
     * @throws ParseException If the node cannot be found and there is no default value
     * @since 1.0.0
     */
    @NotNull
    protected static String find(String name, Element node, String def) throws ParseException {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); ++i) {
            Node n = list.item(i);
            if (n.getNodeName().equals(name)) {
                try {
                    return replaceVariables(n.getTextContent(), node.getOwnerDocument().getDocumentElement());
                } catch (ParseException ex) {
                    if (def == null) {
                        throw ex;
                    } else {
                        return def;
                    }
                }
            }
        }
        list = node.getElementsByTagName(name);
        if (list.getLength() > 0) {
            try {
                return replaceVariables(list.item(0).getTextContent(), node.getOwnerDocument().getDocumentElement());
            } catch (ParseException ex) {
                if (def == null) {
                    throw ex;
                } else {
                    return def;
                }
            }
        }
        if (def == null) {
            throw new ParseException(String.format("Unable to find required tag '%s' in node", name), -1);
        } else {
            return def;
        }
    }
}
