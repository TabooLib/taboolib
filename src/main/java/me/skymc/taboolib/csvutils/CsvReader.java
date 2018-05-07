package me.skymc.taboolib.csvutils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.HashMap;

public class CsvReader {

    private Reader inputStream;
    private String fileName;
    private UserSettings userSettings;
    private Charset charset;
    private boolean useCustomRecordDelimiter;
    private DataBuffer dataBuffer;
    private ColumnBuffer columnBuffer;
    private RawRecordBuffer rawBuffer;
    private boolean[] isQualified;
    private String rawRecord;
    private HeadersHolder headersHolder;
    private boolean startedColumn;
    private boolean startedWithQualifier;
    private boolean hasMoreData;
    private char lastLetter;
    private boolean hasReadNextLine;
    private int columnsCount;
    private long currentRecord;
    private String[] values;
    private boolean initialized;
    private boolean closed;
    public static final int ESCAPE_MODE_DOUBLED = 1;
    public static final int ESCAPE_MODE_BACKSLASH = 2;

    public CsvReader(final String fileName, final char delimiter, final Charset charset) throws FileNotFoundException {
        this.inputStream = null;
        this.fileName = null;
        this.userSettings = new UserSettings();
        this.charset = null;
        this.useCustomRecordDelimiter = false;
        this.dataBuffer = new DataBuffer();
        this.columnBuffer = new ColumnBuffer();
        this.rawBuffer = new RawRecordBuffer();
        this.isQualified = null;
        this.rawRecord = "";
        this.headersHolder = new HeadersHolder();
        this.startedColumn = false;
        this.startedWithQualifier = false;
        this.hasMoreData = true;
        this.lastLetter = '\0';
        this.hasReadNextLine = false;
        this.columnsCount = 0;
        this.currentRecord = 0L;
        this.values = new String[10];
        this.initialized = false;
        this.closed = false;
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName can not be null.");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Parameter charset can not be null.");
        }
        if (!new File(fileName).exists()) {
            throw new FileNotFoundException("File " + fileName + " does not exist.");
        }
        this.fileName = fileName;
        this.userSettings.Delimiter = delimiter;
        this.charset = charset;
        this.isQualified = new boolean[this.values.length];
    }

    public CsvReader(final String s, final char c) throws FileNotFoundException {
        this(s, c, Charset.forName("ISO-8859-1"));
    }

    public CsvReader(final String s) throws FileNotFoundException {
        this(s, ',');
    }

    public CsvReader(final Reader inputStream, final char delimiter) {
        this.inputStream = null;
        this.fileName = null;
        this.userSettings = new UserSettings();
        this.charset = null;
        this.useCustomRecordDelimiter = false;
        this.dataBuffer = new DataBuffer();
        this.columnBuffer = new ColumnBuffer();
        this.rawBuffer = new RawRecordBuffer();
        this.isQualified = null;
        this.rawRecord = "";
        this.headersHolder = new HeadersHolder();
        this.startedColumn = false;
        this.startedWithQualifier = false;
        this.hasMoreData = true;
        this.lastLetter = '\0';
        this.hasReadNextLine = false;
        this.columnsCount = 0;
        this.currentRecord = 0L;
        this.values = new String[10];
        this.initialized = false;
        this.closed = false;
        if (inputStream == null) {
            throw new IllegalArgumentException("Parameter inputStream can not be null.");
        }
        this.inputStream = inputStream;
        this.userSettings.Delimiter = delimiter;
        this.initialized = true;
        this.isQualified = new boolean[this.values.length];
    }

    public CsvReader(final Reader reader) {
        this(reader, ',');
    }

    public CsvReader(final InputStream inputStream, final char c, final Charset charset) {
        this(new InputStreamReader(inputStream, charset), c);
    }

    public CsvReader(final InputStream inputStream, final Charset charset) {
        this(new InputStreamReader(inputStream, charset));
    }

    public boolean getCaptureRawRecord() {
        return this.userSettings.CaptureRawRecord;
    }

    public void setCaptureRawRecord(final boolean captureRawRecord) {
        this.userSettings.CaptureRawRecord = captureRawRecord;
    }

    public String getRawRecord() {
        return this.rawRecord;
    }

    public boolean getTrimWhitespace() {
        return this.userSettings.TrimWhitespace;
    }

    public void setTrimWhitespace(final boolean trimWhitespace) {
        this.userSettings.TrimWhitespace = trimWhitespace;
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

    public char getComment() {
        return this.userSettings.Comment;
    }

    public void setComment(final char comment) {
        this.userSettings.Comment = comment;
    }

    public boolean getUseComments() {
        return this.userSettings.UseComments;
    }

    public void setUseComments(final boolean useComments) {
        this.userSettings.UseComments = useComments;
    }

    public int getEscapeMode() {
        return this.userSettings.EscapeMode;
    }

    public void setEscapeMode(final int escapeMode) throws IllegalArgumentException {
        if (escapeMode != 1 && escapeMode != 2) {
            throw new IllegalArgumentException("Parameter escapeMode must be a valid value.");
        }
        this.userSettings.EscapeMode = escapeMode;
    }

    public boolean getSkipEmptyRecords() {
        return this.userSettings.SkipEmptyRecords;
    }

    public void setSkipEmptyRecords(final boolean skipEmptyRecords) {
        this.userSettings.SkipEmptyRecords = skipEmptyRecords;
    }

    public boolean getSafetySwitch() {
        return this.userSettings.SafetySwitch;
    }

    public void setSafetySwitch(final boolean safetySwitch) {
        this.userSettings.SafetySwitch = safetySwitch;
    }

    public int getColumnCount() {
        return this.columnsCount;
    }

    public long getCurrentRecord() {
        return this.currentRecord - 1L;
    }

    // TODO 2017-11-29 18:38:13 UPDATED
    public long setCurrentRecord(long currentRecord) {
        return this.currentRecord = currentRecord;
    }

    public int getHeaderCount() {
        return this.headersHolder.Length;
    }

    public String[] getHeaders() throws IOException {
        this.checkClosed();
        if (this.headersHolder.Headers == null) {
            return null;
        }
        final String[] array = new String[this.headersHolder.Length];
        System.arraycopy(this.headersHolder.Headers, 0, array, 0, this.headersHolder.Length);
        return array;
    }

    public void setHeaders(final String[] headers) {
        this.headersHolder.Headers = headers;
        this.headersHolder.IndexByName.clear();
        if (headers != null) {
            this.headersHolder.Length = headers.length;
        } else {
            this.headersHolder.Length = 0;
        }
        for (int i = 0; i < this.headersHolder.Length; ++i) {
            this.headersHolder.IndexByName.put(headers[i], i);
        }
    }

    public String[] getValues() throws IOException {
        this.checkClosed();
        final String[] array = new String[this.columnsCount];
        System.arraycopy(this.values, 0, array, 0, this.columnsCount);
        return array;
    }

    public String get(final int n) throws IOException {
        this.checkClosed();
        if (n > -1 && n < this.columnsCount) {
            return this.values[n];
        }
        return "";
    }

    public String get(final String s) throws IOException {
        this.checkClosed();
        return this.get(this.getIndex(s));
    }

    public static CsvReader parse(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("Parameter data can not be null.");
        }
        return new CsvReader(new StringReader(s));
    }

    public boolean readRecord() throws IOException {
        this.checkClosed();
        this.columnsCount = 0;
        this.rawBuffer.Position = 0;
        this.dataBuffer.LineStart = this.dataBuffer.Position;
        this.hasReadNextLine = false;
        if (this.hasMoreData) {
            do {
                if (this.dataBuffer.Position == this.dataBuffer.Count) {
                    this.checkDataLength();
                } else {
                    this.startedWithQualifier = false;
                    char c = this.dataBuffer.Buffer[this.dataBuffer.Position];
                    if (this.userSettings.UseTextQualifier && c == this.userSettings.TextQualifier) {
                        this.lastLetter = c;
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                        this.startedWithQualifier = true;
                        int n = 0;
                        char textQualifier = this.userSettings.TextQualifier;
                        if (this.userSettings.EscapeMode == 2) {
                            textQualifier = '\\';
                        }
                        int n2 = 0;
                        int n3 = 0;
                        int n4 = 0;
                        int n5 = 1;
                        int n6 = 0;
                        char c2 = '\0';
                        final DataBuffer dataBuffer = this.dataBuffer;
                        ++dataBuffer.Position;
                        do {
                            if (this.dataBuffer.Position == this.dataBuffer.Count) {
                                this.checkDataLength();
                            } else {
                                final char lastLetter = this.dataBuffer.Buffer[this.dataBuffer.Position];
                                if (n2 != 0) {
                                    this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                    if (lastLetter == this.userSettings.Delimiter) {
                                        this.endColumn();
                                    } else if ((!this.useCustomRecordDelimiter && (lastLetter == '\r' || lastLetter == '\n')) || (this.useCustomRecordDelimiter && lastLetter == this.userSettings.RecordDelimiter)) {
                                        this.endColumn();
                                        this.endRecord();
                                    }
                                } else if (n4 != 0) {
                                    ++n6;
                                    switch (n5) {
                                        case 1: {
                                            c2 = (char) ((char) (c2 * '\u0010') + hexToDec(lastLetter));
                                            if (n6 == 4) {
                                                n4 = 0;
                                                break;
                                            }
                                            break;
                                        }
                                        case 2: {
                                            c2 = (char) ((char) (c2 * '\b') + (char) (lastLetter - '0'));
                                            if (n6 == 3) {
                                                n4 = 0;
                                                break;
                                            }
                                            break;
                                        }
                                        case 3: {
                                            c2 = (char) ((char) (c2 * '\n') + (char) (lastLetter - '0'));
                                            if (n6 == 3) {
                                                n4 = 0;
                                                break;
                                            }
                                            break;
                                        }
                                        case 4: {
                                            c2 = (char) ((char) (c2 * '\u0010') + hexToDec(lastLetter));
                                            if (n6 == 2) {
                                                n4 = 0;
                                                break;
                                            }
                                            break;
                                        }
                                    }
                                    if (n4 == 0) {
                                        this.appendLetter(c2);
                                    } else {
                                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                    }
                                } else if (lastLetter == this.userSettings.TextQualifier) {
                                    if (n3 != 0) {
                                        n3 = 0;
                                        n = 0;
                                    } else {
                                        this.updateCurrentValue();
                                        if (this.userSettings.EscapeMode == 1) {
                                            n3 = 1;
                                        }
                                        n = 1;
                                    }
                                } else if (this.userSettings.EscapeMode == 2 && n3 != 0) {
                                    switch (lastLetter) {
                                        case 'n': {
                                            this.appendLetter('\n');
                                            break;
                                        }
                                        case 'r': {
                                            this.appendLetter('\r');
                                            break;
                                        }
                                        case 't': {
                                            this.appendLetter('\t');
                                            break;
                                        }
                                        case 'b': {
                                            this.appendLetter('\b');
                                            break;
                                        }
                                        case 'f': {
                                            this.appendLetter('\f');
                                            break;
                                        }
                                        case 'e': {
                                            this.appendLetter('\u001b');
                                            break;
                                        }
                                        case 'v': {
                                            this.appendLetter('\u000b');
                                            break;
                                        }
                                        case 'a': {
                                            this.appendLetter('\u0007');
                                            break;
                                        }
                                        case '0':
                                        case '1':
                                        case '2':
                                        case '3':
                                        case '4':
                                        case '5':
                                        case '6':
                                        case '7': {
                                            n5 = 2;
                                            n4 = 1;
                                            n6 = 1;
                                            c2 = (char) (lastLetter - '0');
                                            this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                            break;
                                        }
                                        case 'D':
                                        case 'O':
                                        case 'U':
                                        case 'X':
                                        case 'd':
                                        case 'o':
                                        case 'u':
                                        case 'x': {
                                            switch (lastLetter) {
                                                case 'U':
                                                case 'u': {
                                                    n5 = 1;
                                                    break;
                                                }
                                                case 'X':
                                                case 'x': {
                                                    n5 = 4;
                                                    break;
                                                }
                                                case 'O':
                                                case 'o': {
                                                    n5 = 2;
                                                    break;
                                                }
                                                case 'D':
                                                case 'd': {
                                                    n5 = 3;
                                                    break;
                                                }
                                            }
                                            n4 = 1;
                                            n6 = 0;
                                            c2 = '\0';
                                            this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                            break;
                                        }
                                    }
                                    n3 = 0;
                                } else if (lastLetter == textQualifier) {
                                    this.updateCurrentValue();
                                    n3 = 1;
                                } else if (n != 0) {
                                    if (lastLetter == this.userSettings.Delimiter) {
                                        this.endColumn();
                                    } else if ((!this.useCustomRecordDelimiter && (lastLetter == '\r' || lastLetter == '\n')) || (this.useCustomRecordDelimiter && lastLetter == this.userSettings.RecordDelimiter)) {
                                        this.endColumn();
                                        this.endRecord();
                                    } else {
                                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                        n2 = 1;
                                    }
                                    n = 0;
                                }
                                this.lastLetter = lastLetter;
                                if (!this.startedColumn) {
                                    continue;
                                }
                                final DataBuffer dataBuffer2 = this.dataBuffer;
                                ++dataBuffer2.Position;
                                if (this.userSettings.SafetySwitch && this.dataBuffer.Position - this.dataBuffer.ColumnStart + this.columnBuffer.Position > 100000) {
                                    this.close();
                                    throw new IOException("Maximum column length of 100,000 exceeded in column " + NumberFormat.getIntegerInstance().format(this.columnsCount) + " in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting column lengths greater than 100,000 characters to" + " avoid this error.");
                                }
                            }
                        } while (this.hasMoreData && this.startedColumn);
                    } else if (c == this.userSettings.Delimiter) {
                        this.lastLetter = c;
                        this.endColumn();
                    } else if (this.useCustomRecordDelimiter && c == this.userSettings.RecordDelimiter) {
                        if (this.startedColumn || this.columnsCount > 0 || !this.userSettings.SkipEmptyRecords) {
                            this.endColumn();
                            this.endRecord();
                        } else {
                            this.dataBuffer.LineStart = this.dataBuffer.Position + 1;
                        }
                        this.lastLetter = c;
                    } else if (!this.useCustomRecordDelimiter && (c == '\r' || c == '\n')) {
                        if (this.startedColumn || this.columnsCount > 0 || (!this.userSettings.SkipEmptyRecords && (c == '\r' || this.lastLetter != '\r'))) {
                            this.endColumn();
                            this.endRecord();
                        } else {
                            this.dataBuffer.LineStart = this.dataBuffer.Position + 1;
                        }
                        this.lastLetter = c;
                    } else if (this.userSettings.UseComments && this.columnsCount == 0 && c == this.userSettings.Comment) {
                        this.lastLetter = c;
                        this.skipLine();
                    } else if (this.userSettings.TrimWhitespace && (c == ' ' || c == '\t')) {
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                    } else {
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = this.dataBuffer.Position;
                        int n7 = 0;
                        int n8 = 0;
                        int n9 = 1;
                        int n10 = 0;
                        char c3 = '\0';
                        int n11 = 1;
                        do {
                            if (n11 == 0 && this.dataBuffer.Position == this.dataBuffer.Count) {
                                this.checkDataLength();
                            } else {
                                if (n11 == 0) {
                                    c = this.dataBuffer.Buffer[this.dataBuffer.Position];
                                }
                                if (!this.userSettings.UseTextQualifier && this.userSettings.EscapeMode == 2 && c == '\\') {
                                    if (n7 != 0) {
                                        n7 = 0;
                                    } else {
                                        this.updateCurrentValue();
                                        n7 = 1;
                                    }
                                } else if (n8 != 0) {
                                    ++n10;
                                    switch (n9) {
                                        case 1: {
                                            c3 = (char) ((char) (c3 * '\u0010') + hexToDec(c));
                                            if (n10 == 4) {
                                                n8 = 0;
                                                break;
                                            }
                                            break;
                                        }
                                        case 2: {
                                            c3 = (char) ((char) (c3 * '\b') + (char) (c - '0'));
                                            if (n10 == 3) {
                                                n8 = 0;
                                                break;
                                            }
                                            break;
                                        }
                                        case 3: {
                                            c3 = (char) ((char) (c3 * '\n') + (char) (c - '0'));
                                            if (n10 == 3) {
                                                n8 = 0;
                                                break;
                                            }
                                            break;
                                        }
                                        case 4: {
                                            c3 = (char) ((char) (c3 * '\u0010') + hexToDec(c));
                                            if (n10 == 2) {
                                                n8 = 0;
                                                break;
                                            }
                                            break;
                                        }
                                    }
                                    if (n8 == 0) {
                                        this.appendLetter(c3);
                                    } else {
                                        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                    }
                                } else if (this.userSettings.EscapeMode == 2 && n7 != 0) {
                                    switch (c) {
                                        case 'n': {
                                            this.appendLetter('\n');
                                            break;
                                        }
                                        case 'r': {
                                            this.appendLetter('\r');
                                            break;
                                        }
                                        case 't': {
                                            this.appendLetter('\t');
                                            break;
                                        }
                                        case 'b': {
                                            this.appendLetter('\b');
                                            break;
                                        }
                                        case 'f': {
                                            this.appendLetter('\f');
                                            break;
                                        }
                                        case 'e': {
                                            this.appendLetter('\u001b');
                                            break;
                                        }
                                        case 'v': {
                                            this.appendLetter('\u000b');
                                            break;
                                        }
                                        case 'a': {
                                            this.appendLetter('\u0007');
                                            break;
                                        }
                                        case '0':
                                        case '1':
                                        case '2':
                                        case '3':
                                        case '4':
                                        case '5':
                                        case '6':
                                        case '7': {
                                            n9 = 2;
                                            n8 = 1;
                                            n10 = 1;
                                            c3 = (char) (c - '0');
                                            this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                            break;
                                        }
                                        case 'D':
                                        case 'O':
                                        case 'U':
                                        case 'X':
                                        case 'd':
                                        case 'o':
                                        case 'u':
                                        case 'x': {
                                            switch (c) {
                                                case 'U':
                                                case 'u': {
                                                    n9 = 1;
                                                    break;
                                                }
                                                case 'X':
                                                case 'x': {
                                                    n9 = 4;
                                                    break;
                                                }
                                                case 'O':
                                                case 'o': {
                                                    n9 = 2;
                                                    break;
                                                }
                                                case 'D':
                                                case 'd': {
                                                    n9 = 3;
                                                    break;
                                                }
                                            }
                                            n8 = 1;
                                            n10 = 0;
                                            c3 = '\0';
                                            this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
                                            break;
                                        }
                                    }
                                    n7 = 0;
                                } else if (c == this.userSettings.Delimiter) {
                                    this.endColumn();
                                } else if ((!this.useCustomRecordDelimiter && (c == '\r' || c == '\n')) || (this.useCustomRecordDelimiter && c == this.userSettings.RecordDelimiter)) {
                                    this.endColumn();
                                    this.endRecord();
                                }
                                this.lastLetter = c;
                                n11 = 0;
                                if (!this.startedColumn) {
                                    continue;
                                }
                                final DataBuffer dataBuffer3 = this.dataBuffer;
                                ++dataBuffer3.Position;
                                if (this.userSettings.SafetySwitch && this.dataBuffer.Position - this.dataBuffer.ColumnStart + this.columnBuffer.Position > 100000) {
                                    this.close();
                                    throw new IOException("Maximum column length of 100,000 exceeded in column " + NumberFormat.getIntegerInstance().format(this.columnsCount) + " in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting column lengths greater than 100,000 characters to" + " avoid this error.");
                                }
                            }
                        } while (this.hasMoreData && this.startedColumn);
                    }
                    if (!this.hasMoreData) {
                        continue;
                    }
                    final DataBuffer dataBuffer4 = this.dataBuffer;
                    ++dataBuffer4.Position;
                }
            } while (this.hasMoreData && !this.hasReadNextLine);
            if (this.startedColumn || this.lastLetter == this.userSettings.Delimiter) {
                this.endColumn();
                this.endRecord();
            }
        }
        if (this.userSettings.CaptureRawRecord) {
            if (this.hasMoreData) {
                if (this.rawBuffer.Position == 0) {
                    this.rawRecord = new String(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.dataBuffer.Position - this.dataBuffer.LineStart - 1);
                } else {
                    this.rawRecord = new String(this.rawBuffer.Buffer, 0, this.rawBuffer.Position) + new String(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.dataBuffer.Position - this.dataBuffer.LineStart - 1);
                }
            } else {
                this.rawRecord = new String(this.rawBuffer.Buffer, 0, this.rawBuffer.Position);
            }
        } else {
            this.rawRecord = "";
        }
        return this.hasReadNextLine;
    }

    private void checkDataLength() throws IOException {
        if (!this.initialized) {
            if (this.fileName != null) {
                this.inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName), this.charset), 4096);
            }
            this.charset = null;
            this.initialized = true;
        }
        this.updateCurrentValue();
        if (this.userSettings.CaptureRawRecord && this.dataBuffer.Count > 0) {
            if (this.rawBuffer.Buffer.length - this.rawBuffer.Position < this.dataBuffer.Count - this.dataBuffer.LineStart) {
                final char[] buffer = new char[this.rawBuffer.Buffer.length + Math.max(this.dataBuffer.Count - this.dataBuffer.LineStart, this.rawBuffer.Buffer.length)];
                System.arraycopy(this.rawBuffer.Buffer, 0, buffer, 0, this.rawBuffer.Position);
                this.rawBuffer.Buffer = buffer;
            }
            System.arraycopy(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.rawBuffer.Buffer, this.rawBuffer.Position, this.dataBuffer.Count - this.dataBuffer.LineStart);
            final RawRecordBuffer rawBuffer = this.rawBuffer;
            rawBuffer.Position += this.dataBuffer.Count - this.dataBuffer.LineStart;
        }
        try {
            this.dataBuffer.Count = this.inputStream.read(this.dataBuffer.Buffer, 0, this.dataBuffer.Buffer.length);
        } catch (IOException ex) {
            this.close();
            throw ex;
        }
        if (this.dataBuffer.Count == -1) {
            this.hasMoreData = false;
        }
        this.dataBuffer.Position = 0;
        this.dataBuffer.LineStart = 0;
        this.dataBuffer.ColumnStart = 0;
    }

    public boolean readHeaders() throws IOException {
        final boolean record = this.readRecord();
        this.headersHolder.Length = this.columnsCount;
        this.headersHolder.Headers = new String[this.columnsCount];
        for (int i = 0; i < this.headersHolder.Length; ++i) {
            final String value = this.get(i);
            this.headersHolder.Headers[i] = value;
            this.headersHolder.IndexByName.put(value, i);
        }
        if (record) {
            --this.currentRecord;
        }
        this.columnsCount = 0;
        return record;
    }

    public String getHeader(final int n) throws IOException {
        this.checkClosed();
        if (n > -1 && n < this.headersHolder.Length) {
            return this.headersHolder.Headers[n];
        }
        return "";
    }

    public boolean isQualified(final int n) throws IOException {
        this.checkClosed();
        return n < this.columnsCount && n > -1 && this.isQualified[n];
    }

    public void endColumn() throws IOException {
        String s = "";
        if (this.startedColumn) {
            if (this.columnBuffer.Position == 0) {
                if (this.dataBuffer.ColumnStart < this.dataBuffer.Position) {
                    int n = this.dataBuffer.Position - 1;
                    if (this.userSettings.TrimWhitespace && !this.startedWithQualifier) {
                        while (n >= this.dataBuffer.ColumnStart && (this.dataBuffer.Buffer[n] == ' ' || this.dataBuffer.Buffer[n] == '\t')) {
                            --n;
                        }
                    }
                    s = new String(this.dataBuffer.Buffer, this.dataBuffer.ColumnStart, n - this.dataBuffer.ColumnStart + 1);
                }
            } else {
                this.updateCurrentValue();
                int n2 = this.columnBuffer.Position - 1;
                if (this.userSettings.TrimWhitespace && !this.startedWithQualifier) {
                    while (n2 >= 0 && (this.columnBuffer.Buffer[n2] == ' ' || this.columnBuffer.Buffer[n2] == ' ')) {
                        --n2;
                    }
                }
                s = new String(this.columnBuffer.Buffer, 0, n2 + 1);
            }
        }
        this.columnBuffer.Position = 0;
        this.startedColumn = false;
        if (this.columnsCount >= 100000 && this.userSettings.SafetySwitch) {
            this.close();
            throw new IOException("Maximum column count of 100,000 exceeded in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting more than 100,000 columns per record to" + " avoid this error.");
        }
        if (this.columnsCount == this.values.length) {
            final int n3 = this.values.length * 2;
            final String[] values = new String[n3];
            System.arraycopy(this.values, 0, values, 0, this.values.length);
            this.values = values;
            final boolean[] isQualified = new boolean[n3];
            System.arraycopy(this.isQualified, 0, isQualified, 0, this.isQualified.length);
            this.isQualified = isQualified;
        }
        this.values[this.columnsCount] = s;
        this.isQualified[this.columnsCount] = this.startedWithQualifier;
        ++this.columnsCount;
    }

    private void appendLetter(final char c) {
        if (this.columnBuffer.Position == this.columnBuffer.Buffer.length) {
            final char[] buffer = new char[this.columnBuffer.Buffer.length * 2];
            System.arraycopy(this.columnBuffer.Buffer, 0, buffer, 0, this.columnBuffer.Position);
            this.columnBuffer.Buffer = buffer;
        }
        this.columnBuffer.Buffer[this.columnBuffer.Position++] = c;
        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
    }

    private void updateCurrentValue() {
        if (this.startedColumn && this.dataBuffer.ColumnStart < this.dataBuffer.Position) {
            if (this.columnBuffer.Buffer.length - this.columnBuffer.Position < this.dataBuffer.Position - this.dataBuffer.ColumnStart) {
                final char[] buffer = new char[this.columnBuffer.Buffer.length + Math.max(this.dataBuffer.Position - this.dataBuffer.ColumnStart, this.columnBuffer.Buffer.length)];
                System.arraycopy(this.columnBuffer.Buffer, 0, buffer, 0, this.columnBuffer.Position);
                this.columnBuffer.Buffer = buffer;
            }
            System.arraycopy(this.dataBuffer.Buffer, this.dataBuffer.ColumnStart, this.columnBuffer.Buffer, this.columnBuffer.Position, this.dataBuffer.Position - this.dataBuffer.ColumnStart);
            final ColumnBuffer columnBuffer = this.columnBuffer;
            columnBuffer.Position += this.dataBuffer.Position - this.dataBuffer.ColumnStart;
        }
        this.dataBuffer.ColumnStart = this.dataBuffer.Position + 1;
    }

    public void endRecord() {
        this.hasReadNextLine = true;
        ++this.currentRecord;
    }

    public int getIndex(final String s) throws IOException {
        this.checkClosed();
        final Integer value = this.headersHolder.IndexByName.get(s);
        if (value != null) {
            return value;
        }
        return -1;
    }

    public boolean skipRecord() throws IOException {
        this.checkClosed();
        boolean record = false;
        if (this.hasMoreData) {
            record = this.readRecord();
            if (record) {
                --this.currentRecord;
            }
        }
        return record;
    }

    public boolean skipLine() throws IOException {
        this.checkClosed();
        this.columnsCount = 0;
        boolean b = false;
        if (this.hasMoreData) {
            boolean b2 = false;
            do {
                if (this.dataBuffer.Position == this.dataBuffer.Count) {
                    this.checkDataLength();
                } else {
                    b = true;
                    final char lastLetter = this.dataBuffer.Buffer[this.dataBuffer.Position];
                    if (lastLetter == '\r' || lastLetter == '\n') {
                        b2 = true;
                    }
                    this.lastLetter = lastLetter;
                    if (b2) {
                        continue;
                    }
                    final DataBuffer dataBuffer = this.dataBuffer;
                    ++dataBuffer.Position;
                }
            } while (this.hasMoreData && !b2);
            this.columnBuffer.Position = 0;
            this.dataBuffer.LineStart = this.dataBuffer.Position + 1;
        }
        this.rawBuffer.Position = 0;
        this.rawRecord = "";
        return b;
    }

    public void close() {
        if (!this.closed) {
            this.close(true);
            this.closed = true;
        }
    }

    private void close(final boolean b) {
        if (!this.closed) {
            if (b) {
                this.charset = null;
                this.headersHolder.Headers = null;
                this.headersHolder.IndexByName = null;
                this.dataBuffer.Buffer = null;
                this.columnBuffer.Buffer = null;
                this.rawBuffer.Buffer = null;
            }
            try {
                if (this.initialized) {
                    this.inputStream.close();
                }
            } catch (Exception ignored) {
            }
            this.inputStream = null;
            this.closed = true;
        }
    }

    private void checkClosed() throws IOException {
        if (this.closed) {
            throw new IOException("This instance of the CsvReader class has already been closed.");
        }
    }

    @Override
    protected void finalize() {
        this.close(false);
    }

    private static char hexToDec(final char c) {
        char c2;
        if (c >= 'a') {
            c2 = (char) (c - 'a' + '\n');
        } else if (c >= 'A') {
            c2 = (char) (c - 'A' + '\n');
        } else {
            c2 = (char) (c - '0');
        }
        return c2;
    }

    private class StaticSettings {
        public static final int MAX_BUFFER_SIZE = 1024;
        public static final int MAX_FILE_BUFFER_SIZE = 4096;
        public static final int INITIAL_COLUMN_COUNT = 10;
        public static final int INITIAL_COLUMN_BUFFER_SIZE = 50;
    }

    private class HeadersHolder {
        public String[] Headers;
        public int Length;
        public HashMap<String, Integer> IndexByName;

        public HeadersHolder() {
            this.Headers = null;
            this.Length = 0;
            this.IndexByName = new HashMap<>();
        }
    }

    private class UserSettings {
        public boolean CaseSensitive;
        public char TextQualifier;
        public boolean TrimWhitespace;
        public boolean UseTextQualifier;
        public char Delimiter;
        public char RecordDelimiter;
        public char Comment;
        public boolean UseComments;
        public int EscapeMode;
        public boolean SafetySwitch;
        public boolean SkipEmptyRecords;
        public boolean CaptureRawRecord;

        public UserSettings() {
            this.CaseSensitive = true;
            this.TextQualifier = '\"';
            this.TrimWhitespace = true;
            this.UseTextQualifier = true;
            this.Delimiter = ',';
            this.RecordDelimiter = '\0';
            this.Comment = '#';
            this.UseComments = false;
            this.EscapeMode = 1;
            this.SafetySwitch = true;
            this.SkipEmptyRecords = true;
            this.CaptureRawRecord = true;
        }
    }

    private class Letters {
        public static final char LF = '\n';
        public static final char CR = '\r';
        public static final char QUOTE = '\"';
        public static final char COMMA = ',';
        public static final char SPACE = ' ';
        public static final char TAB = '\t';
        public static final char POUND = '#';
        public static final char BACKSLASH = '\\';
        public static final char NULL = '\0';
        public static final char BACKSPACE = '\b';
        public static final char FORM_FEED = '\f';
        public static final char ESCAPE = '\u001b';
        public static final char VERTICAL_TAB = '\u000b';
        public static final char ALERT = '\u0007';
    }

    private class RawRecordBuffer {
        public char[] Buffer;
        public int Position;

        public RawRecordBuffer() {
            this.Buffer = new char[500];
            this.Position = 0;
        }
    }

    private class ColumnBuffer {
        public char[] Buffer;
        public int Position;

        public ColumnBuffer() {
            this.Buffer = new char[50];
            this.Position = 0;
        }
    }

    private class DataBuffer {
        public char[] Buffer;
        public int Position;
        public int Count;
        public int ColumnStart;
        public int LineStart;

        public DataBuffer() {
            this.Buffer = new char[1024];
            this.Position = 0;
            this.Count = 0;
            this.ColumnStart = 0;
            this.LineStart = 0;
        }
    }

    private class ComplexEscape {
        private static final int UNICODE = 1;
        private static final int OCTAL = 2;
        private static final int DECIMAL = 3;
        private static final int HEX = 4;
    }
}
