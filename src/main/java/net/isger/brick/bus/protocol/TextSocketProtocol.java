package net.isger.brick.bus.protocol;

import net.isger.brick.Constants;
import net.isger.util.Decoder;
import net.isger.util.Encoder;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Alias("text.socket")
public class TextSocketProtocol implements SocketProtocol {

    public static final String DELIMITER = "\n";

    private volatile transient Status status;

    @Alias(Constants.BRICK_ENCODING)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    private String encoding;

    private String sourceCharset;

    private String targetCharset;

    private String delimiter;

    private transient TextSocketEncoder encoder;

    private transient TextSocketDecoder decoder;

    public TextSocketProtocol() {
        this.status = Status.UNINITIALIZED;
        this.delimiter = DELIMITER;
    }

    public boolean hasReady() {
        return this.status == Status.INITIALIZED;
    }

    public Status getStatus() {
        return this.status;
    }

    public synchronized final void initial() {
        if (!(status == Status.UNINITIALIZED || status == Status.DESTROYED)) return;
        this.status = Status.INITIALIZING;
        this.sourceCharset = Strings.empty(this.sourceCharset, this.encoding);
        this.targetCharset = Strings.empty(this.targetCharset, this.encoding);
        if (this.encoder == null) {
            this.encoder = createEncoder(this.sourceCharset, this.targetCharset, this.delimiter);
        }
        if (this.decoder == null) {
            this.decoder = createDecoder(this.targetCharset, this.sourceCharset, this.delimiter);
        }
        this.status = Status.INITIALIZED;
    }

    protected TextSocketEncoder createEncoder(String sourceCharset, String targetCharset, String delimiter) {
        return new TextSocketEncoder(sourceCharset, targetCharset, delimiter);
    }

    protected TextSocketDecoder createDecoder(String sourceCharset, String targetCharset, String delimiter) {
        return new TextSocketDecoder(sourceCharset, targetCharset, delimiter);
    }

    public Encoder getEncoder() {
        return this.encoder;
    }

    public Decoder getDecoder() {
        return this.decoder;
    }

    public synchronized void destroy() {
        if (this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED) return;
        this.status = Status.DESTROYED;
    }

}
