package me.skymc.taboolib.csvutils;

import java.io.*;
import java.nio.charset.Charset;

public class CsvWriter
{
    private Writer outputStream;
    private String fileName;
    private boolean firstColumn;
    private boolean useCustomRecordDelimiter;
    private Charset charset;
    private UserSettings userSettings;
    private boolean initialized;
    private boolean closed;
    private String systemRecordDelimiter;
    public static final int ESCAPE_MODE_DOUBLED = 1;
    public static final int ESCAPE_MODE_BACKSLASH = 2;
    
    public CsvWriter(final String fileName, final char delimiter, final Charset charset) {
        this.outputStream = null;
        this.fileName = null;
        this.firstColumn = true;
        this.useCustomRecordDelimiter = false;
        this.charset = null;
        this.userSettings = new UserSettings();
        this.initialized = false;
        this.closed = false;
        this.systemRecordDelimiter = System.getProperty("line.separator");
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName can not be null.");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Parameter charset can not be null.");
        }
        this.fileName = fileName;
        this.userSettings.Delimiter = delimiter;
        this.charset = charset;
    }
    
    public CsvWriter(final String s) {
        this(s, ',', Charset.forName("ISO-8859-1"));
    }
    
    public CsvWriter(final Writer outputStream, final char delimiter) {
        this.outputStream = null;
        this.fileName = null;
        this.firstColumn = true;
        this.useCustomRecordDelimiter = false;
        this.charset = null;
        this.userSettings = new UserSettings();
        this.initialized = false;
        this.closed = false;
        this.systemRecordDelimiter = System.getProperty("line.separator");
        if (outputStream == null) {
            throw new IllegalArgumentException("Parameter outputStream can not be null.");
        }
        this.outputStream = outputStream;
        this.userSettings.Delimiter = delimiter;
        this.initialized = true;
    }
    
    public CsvWriter(final OutputStream outputStream, final char c, final Charset charset) {
        this(new OutputStreamWriter(outputStream, charset), c);
    }
    
    public char getDelimiter() {
        return this.userSettings.Delimiter;
    }
    
    public void setDelimiter(final char delimiter) {
        this.userSettings.Delimiter = delimiter;
    }
    
    public char getRecordDelimiter() {
        return this.userSettings.RecordDelimiter;
    }
    
    public void setRecordDelimiter(final char recordDelimiter) {
        this.useCustomRecordDelimiter = true;
        this.userSettings.RecordDelimiter = recordDelimiter;
    }
    
    public char getTextQualifier() {
        return this.userSettings.TextQualifier;
    }
    
    public void setTextQualifier(final char textQualifier) {
        this.userSettings.TextQualifier = textQualifier;
    }
    
    public boolean getUseTextQualifier() {
        return this.userSettings.UseTextQualifier;
    }
    
    public void setUseTextQualifier(final boolean useTextQualifier) {
        this.userSettings.UseTextQualifier = useTextQualifier;
    }
    
    public int getEscapeMode() {
        return this.userSettings.EscapeMode;
    }
    
    public void setEscapeMode(final int escapeMode) {
        this.userSettings.EscapeMode = escapeMode;
    }
    
    public void setComment(final char comment) {
        this.userSettings.Comment = comment;
    }
    
    public char getComment() {
        return this.userSettings.Comment;
    }
    
    public boolean getForceQualifier() {
        return this.userSettings.ForceQualifier;
    }
    
    public void setForceQualifier(final boolean forceQualifier) {
        this.userSettings.ForceQualifier = forceQualifier;
    }
    
    public void write(String s, final boolean b) throws IOException {
        this.checkClosed();
        this.checkInit();
        if (s == null) {
            s = "";
        }
        if (!this.firstColumn) {
            this.outputStream.write(this.userSettings.Delimiter);
        }
        int forceQualifier = this.userSettings.ForceQualifier ? 1 : 0;
        if (!b && s.length() > 0) {
            s = s.trim();
        }
        if (forceQualifier == 0 && this.userSettings.UseTextQualifier && (s.indexOf(this.userSettings.TextQualifier) > -1 || s.indexOf(this.userSettings.Delimiter) > -1 || (!this.useCustomRecordDelimiter && (s.indexOf(10) > -1 || s.indexOf(13) > -1)) || (this.useCustomRecordDelimiter && s.indexOf(this.userSettings.RecordDelimiter) > -1) || (this.firstColumn && s.length() > 0 && s.charAt(0) == this.userSettings.Comment) || (this.firstColumn && s.length() == 0))) {
            forceQualifier = 1;
        }
        if (this.userSettings.UseTextQualifier && forceQualifier == 0 && s.length() > 0 && b) {
            final char char1 = s.charAt(0);
            if (char1 == ' ' || char1 == '\t') {
                forceQualifier = 1;
            }
            if (forceQualifier == 0 && s.length() > 1) {
                final char char2 = s.charAt(s.length() - 1);
                if (char2 == ' ' || char2 == '\t') {
                    forceQualifier = 1;
                }
            }
        }
        if (forceQualifier != 0) {
            this.outputStream.write(this.userSettings.TextQualifier);
            if (this.userSettings.EscapeMode == 2) {
                s = replace(s, "\\", "\\\\");
                s = replace(s, "" + this.userSettings.TextQualifier, "\\" + this.userSettings.TextQualifier);
            }
            else {
                s = replace(s, "" + this.userSettings.TextQualifier, "" + this.userSettings.TextQualifier + this.userSettings.TextQualifier);
            }
        }
        else if (this.userSettings.EscapeMode == 2) {
            s = replace(s, "\\", "\\\\");
            s = replace(s, "" + this.userSettings.Delimiter, "\\" + this.userSettings.Delimiter);
            if (this.useCustomRecordDelimiter) {
                s = replace(s, "" + this.userSettings.RecordDelimiter, "\\" + this.userSettings.RecordDelimiter);
            }
            else {
                s = replace(s, "\r", "\\\r");
                s = replace(s, "\n", "\\\n");
            }
            if (this.firstColumn && s.length() > 0 && s.charAt(0) == this.userSettings.Comment) {
                if (s.length() > 1) {
                    s = "\\" + this.userSettings.Comment + s.substring(1);
                }
                else {
                    s = "\\" + this.userSettings.Comment;
                }
            }
        }
        this.outputStream.write(s);
        if (forceQualifier != 0) {
            this.outputStream.write(this.userSettings.TextQualifier);
        }
        this.firstColumn = false;
    }
    
    public void write(final String s) throws IOException {
        this.write(s, false);
    }
    
    public void writeComment(final String s) throws IOException {
        this.checkClosed();
        this.checkInit();
        this.outputStream.write(this.userSettings.Comment);
        this.outputStream.write(s);
        if (this.useCustomRecordDelimiter) {
            this.outputStream.write(this.userSettings.RecordDelimiter);
        }
        else {
            this.outputStream.write(this.systemRecordDelimiter);
        }
        this.firstColumn = true;
    }

    public static String replace(final String s, final String s2, final String s3) {
        final int length = s2.length();
        int i = s.indexOf(s2);
        if (i > -1) {
            final StringBuilder sb = new StringBuilder();
            int n;
            for (n = 0; i != -1; i = s.indexOf(s2, n)) {
                sb.append(s.substring(n, i));
                sb.append(s3);
                n = i + length;
            }
            sb.append(s.substring(n));
            return sb.toString();
        }
        return s;
    }
    
    public void writeRecord(final String[] array) throws IOException {
        this.writeRecord(array, false);
    }
    
    public void endRecord() throws IOException {
        this.checkClosed();
        this.checkInit();
        if (this.useCustomRecordDelimiter) {
            this.outputStream.write(this.userSettings.RecordDelimiter);
        }
        else {
            this.outputStream.write(this.systemRecordDelimiter);
        }
        this.firstColumn = true;
    }
    
    private void checkInit() throws IOException {
        if (!this.initialized) {
            if (this.fileName != null) {
                this.outputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.fileName), this.charset));
            }
            this.initialized = true;
        }
    }
    
    public void flush() throws IOException {
        this.outputStream.flush();
    }
    
    public void close() {
        if (!this.closed) {
            this.close(true);
            this.closed = true;
        }
    }

    public void writeRecord(final String[] array, final boolean b) throws IOException {
        if (array != null && array.length > 0) {
            for (String anArray : array) {
                this.write(anArray, b);
            }
            this.endRecord();
        }
    }

    private void checkClosed() throws IOException {
        if (this.closed) {
            throw new IOException("This instance of the CsvWriter class has already been closed.");
        }
    }

    protected void finalize() {
        this.close(false);
    }
    
    private void close(final boolean b) {
        if (!this.closed) {
            if (b) {
                this.charset = null;
            }
            try {
                if (this.initialized) {
                    this.outputStream.close();
                }
            } catch (Exception ignored) {
            }
            this.outputStream = null;
            this.closed = true;
        }
    }
    
    private class UserSettings
    {
        public char TextQualifier;
        public boolean UseTextQualifier;
        public char Delimiter;
        public char RecordDelimiter;
        public char Comment;
        public int EscapeMode;
        public boolean ForceQualifier;
        
        public UserSettings() {
            this.TextQualifier = '\"';
            this.UseTextQualifier = true;
            this.Delimiter = ',';
            this.RecordDelimiter = '\0';
            this.Comment = '#';
            this.EscapeMode = 1;
            this.ForceQualifier = false;
        }
    }
    
    private class Letters
    {
        public static final char LF = '\n';
        public static final char CR = '\r';
        public static final char QUOTE = '\"';
        public static final char COMMA = ',';
        public static final char SPACE = ' ';
        public static final char TAB = '\t';
        public static final char POUND = '#';
        public static final char BACKSLASH = '\\';
        public static final char NULL = '\0';
    }
}
