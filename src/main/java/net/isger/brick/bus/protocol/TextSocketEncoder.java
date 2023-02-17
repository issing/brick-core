package net.isger.brick.bus.protocol;

import java.io.UnsupportedEncodingException;

import net.isger.brick.Constants;
import net.isger.brick.bus.protocol.SocketProtocol.Encoder;
import net.isger.util.Strings;

public class TextSocketEncoder implements Encoder {

    private String sourceCharset;

    private String targetCharset;

    private String delimiter;

    public TextSocketEncoder() {
        this(Constants.ENC_UTF8, Constants.ENC_UTF8,
                TextSocketProtocol.DELIMITER);
    }

    public TextSocketEncoder(String sourceCharset, String targetCharset,
            String delimiter) {
        this.sourceCharset = sourceCharset;
        this.targetCharset = targetCharset;
        this.delimiter = delimiter;
    }

    public byte[] encode(Object message) {
        byte[] data = null;
        if (message != null) {
            try {
                data = (message + delimiter).getBytes(sourceCharset);
                if (!sourceCharset.equalsIgnoreCase(targetCharset)) {
                    data = Strings.toCharset(data, sourceCharset, targetCharset)
                            .getBytes(targetCharset);
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
        return data;
    }

    public String getSourceCharset() {
        return sourceCharset;
    }

    public String getTargetCharset() {
        return targetCharset;
    }

    public final String getEncoding() {
        return getSourceCharset();
    }

    public String getDelimiter() {
        return delimiter;
    }

}
