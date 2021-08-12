package taboolib.library.kether;

public abstract class AbstractStringReader {

    protected final char[] content;
    protected int index = 0;
    protected int mark = 0;

    public AbstractStringReader(char[] content) {
        this.content = content;
    }

    public AbstractStringReader(char[] content, int index, int mark) {
        this.content = content;
        this.index = index;
        this.mark = mark;
    }

    public char peek() {
        if (index < content.length) {
            return content[index];
        } else {
            throw LoadError.EOF.create();
        }
    }

    public char peek(int n) {
        if (index + n < content.length) {
            return content[index + n];
        } else {
            throw LoadError.EOF.create();
        }
    }

    public boolean hasNext() {
        skipBlank();
        return index < content.length;
    }

    public void mark() {
        this.mark = index;
    }

    public void reset() {
        this.index = mark;
    }

    public String nextToken() {
        if (!hasNext()) {
            throw LoadError.EOF.create();
        }
        int begin = index;
        while (index < content.length && !Character.isWhitespace(content[index])) {
            index++;
        }
        return new String(content, begin, index - begin);
    }

    protected void skip(int n) {
        index += n;
    }

    protected void skipBlank() {
        while (index < content.length) {
            if (Character.isWhitespace(content[index])) {
                index++;
            } else if (index + 1 < content.length && content[index] == '/' && content[index + 1] == '/') {
                while (index < content.length && content[index] != '\n' && content[index] != '\r') {
                    index++;
                }
            } else {
                break;
            }
        }
    }

    protected void expect(String value) {
        String element = nextToken();
        if (!element.equals(value)) {
            failExpect(value, element);
        }
    }

    protected void failExpect(String expect, String got) {
        throw LoadError.NOT_MATCH.create(expect, got);
    }

    public char[] getContent() {
        return content;
    }

    public int getIndex() {
        return index;
    }

    public int getMark() {
        return mark;
    }
}
