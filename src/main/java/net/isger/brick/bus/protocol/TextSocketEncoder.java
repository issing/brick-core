package net.isger.brick.bus.protocol;

import java.io.UnsupportedEncodingException;

import net.isger.brick.Constants;
import net.isger.util.Encoder;
import net.isger.util.Strings;

public class TextSocketEncoder implements Encoder {

    private String sourceCharset;

    private String targetCharset;

    private String delimiter;

    public TextSocketEncoder() {
        this(Constants.ENCODING_UTF_8, Constants.ENCODING_UTF_8, TextSocketProtocol.DELIMITER);
    }

    public TextSocketEncoder(String sourceCharset, String targetCharset, String delimiter) {
        this.sourceCharset = sourceCharset;
        this.targetCharset = targetCharset;
        this.delimiter = delimiter;
    }

    public byte[] encode(Object message) {
        byte[] data = null;
        if (message != null) {
            try {
                data = (message + this.delimiter).getBytes(this.sourceCharset);
                if (!this.sourceCharset.equalsIgnoreCase(this.targetCharset)) {
                    data = Strings.toCharset(data, this.sourceCharset, this.targetCharset).getBytes(this.targetCharset);
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
        return data;
    }

    public String getSourceCharset() {
        return this.sourceCharset;
    }

    public String getTargetCharset() {
        return this.targetCharset;
    }

    public final String getEncoding() {
        return this.getSourceCharset();
    }

    public String getDelimiter() {
        return this.delimiter;
    }

}
