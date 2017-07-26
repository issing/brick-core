package net.isger.brick.bus.protocol;

import net.isger.brick.Constants;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;

@Alias("text.socket")
public class TextSocketProtocol implements SocketProtocol {

    public static final String DELIMITER = "\n";

    @Alias(Constants.BRICK_ENCODING)
    private String encoding;

    private String sourceCharset;

    private String targetCharset;

    private String delimiter;

    private transient TextSocketEncoder encoder;

    private transient TextSocketDecoder decoder;

    public TextSocketProtocol() {
        delimiter = DELIMITER;
    }

    public final void initial() {
        sourceCharset = Strings.empty(sourceCharset, encoding);
        targetCharset = Strings.empty(targetCharset, encoding);
        if (encoder == null) {
            encoder = createEncoder(sourceCharset, targetCharset, delimiter);
        }
        if (decoder == null) {
            decoder = createDecoder(targetCharset, sourceCharset, delimiter);
        }
    }

    protected TextSocketEncoder createEncoder(String sourceCharset,
            String targetCharset, String delimiter) {
        return new TextSocketEncoder(sourceCharset, targetCharset, delimiter);
    }

    protected TextSocketDecoder createDecoder(String sourceCharset,
            String targetCharset, String delimiter) {
        return new TextSocketDecoder(sourceCharset, targetCharset, delimiter);
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
