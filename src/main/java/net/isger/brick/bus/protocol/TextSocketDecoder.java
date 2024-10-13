package net.isger.brick.bus.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.isger.brick.Constants;
import net.isger.util.Asserts;
import net.isger.util.Decoder;
import net.isger.util.Strings;

public class TextSocketDecoder implements Decoder {

    private String sourceCharset;

    private String targetCharset;

    private transient byte[] delimiter;

    public TextSocketDecoder() {
        this(Constants.ENCODING_UTF_8, Constants.ENCODING_UTF_8, TextSocketProtocol.DELIMITER);
    }

    public TextSocketDecoder(String sourceCharset, String targetCharset, String delimiter) {
        this.sourceCharset = sourceCharset;
        this.targetCharset = targetCharset;
        try {
            this.delimiter = delimiter.getBytes(sourceCharset);
        } catch (UnsupportedEncodingException e) {
            throw Asserts.argument("Unsupported encoding [{}]", sourceCharset);
        }
    }

    public Object decode(byte[] content) {
        return decode(new ByteArrayInputStream(content));
    }

    public Object decode(InputStream is) {
        is.mark(0);
        try {
            String value;
            byte[] data = new byte[Math.max(is.available(), this.delimiter.length)];
            int size = is.read(data) - this.delimiter.length;
            int i = 0;
            next: while (i++ < size) {
                int j = 0;
                do {
                    if (data[i + j] != this.delimiter[j]) {
                        continue next;
                    }
                } while (++j < this.delimiter.length);
                is.reset();
                is.skip(i + j);
                value = new String(data, 0, i, sourceCharset).trim();
                if (value.length() == 0 || value.equals(new String(this.delimiter, this.targetCharset))) {
                    is.mark(0);
                    continue;
                }
                return Strings.toCharset(value.getBytes(this.sourceCharset), this.sourceCharset, this.targetCharset);
            }
            is.reset();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    public String getSourceCharset() {
        return sourceCharset;
    }

    public String getTargetCharset() {
        return targetCharset;
    }

    public String getEncoding() {
        return this.getTargetCharset();
    }

    public byte[] getDelimiter() {
        return delimiter;
    }

}
