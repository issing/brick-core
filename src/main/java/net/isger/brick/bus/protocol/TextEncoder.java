package net.isger.brick.bus.protocol;

import java.io.UnsupportedEncodingException;

import net.isger.brick.Constants;
import net.isger.brick.bus.Encoder;

public class TextEncoder implements Encoder {

    private String encoding;

    private String delimiter;

    public TextEncoder() {
        this(Constants.ENCODING_UTF_8, TextProtocol.DELIMITER);
    }

    public TextEncoder(String encoding, String delimiter) {
        this.encoding = encoding;
        this.delimiter = delimiter;
    }

    public byte[] encode(Object message) {
        try {
            return message == null ? null : (message + delimiter)
                    .getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

}
