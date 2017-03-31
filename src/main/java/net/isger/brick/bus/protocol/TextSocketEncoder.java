package net.isger.brick.bus.protocol;

import java.io.UnsupportedEncodingException;

import net.isger.brick.Constants;
import net.isger.brick.bus.protocol.SocketProtocol.Encoder;

public class TextSocketEncoder implements Encoder {

    private String encoding;

    private String delimiter;

    public TextSocketEncoder() {
        this(Constants.DEFAULT_ENCODING, TextSocketProtocol.DELIMITER);
    }

    public TextSocketEncoder(String encoding, String delimiter) {
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
