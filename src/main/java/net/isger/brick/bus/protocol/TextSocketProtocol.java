package net.isger.brick.bus.protocol;

import net.isger.brick.Constants;
import net.isger.util.anno.Alias;

@Alias("text.socket")
public class TextSocketProtocol implements SocketProtocol {

    public static final String DELIMITER = "\n";

    private String encoding;

    private String delimiter;

    private transient TextSocketEncoder encoder;

    private transient TextSocketDecoder decoder;

    public TextSocketProtocol() {
        encoding = Constants.DEFAULT_ENCODING;
        delimiter = DELIMITER;
    }

    public void initial() {
        if (encoder == null) {
            encoder = new TextSocketEncoder(encoding, delimiter);
        }
        if (decoder == null) {
            decoder = new TextSocketDecoder(encoding, delimiter);
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
