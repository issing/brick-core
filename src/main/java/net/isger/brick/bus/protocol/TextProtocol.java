package net.isger.brick.bus.protocol;

import net.isger.brick.Constants;
import net.isger.brick.bus.Decoder;
import net.isger.brick.bus.Encoder;

public class TextProtocol implements Protocol {

    public static final String DELIMITER = "\n";

    private String encoding;

    private String delimiter;

    private transient TextEncoder encoder;

    private transient TextDecoder decoder;

    public TextProtocol() {
        encoding = Constants.DEFAULT_ENCODING;
        delimiter = DELIMITER;
    }

    public void initial() {
        if (encoder == null) {
            encoder = new TextEncoder(encoding, delimiter);
        }
        if (decoder == null) {
            decoder = new TextDecoder(encoding, delimiter);
        }
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public void destroy() {
    }

}
