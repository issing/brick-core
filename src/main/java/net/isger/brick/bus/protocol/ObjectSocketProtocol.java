package net.isger.brick.bus.protocol;

import net.isger.util.Decoder;
import net.isger.util.Encoder;
import net.isger.util.anno.Alias;

@Alias("object.socket")
public class ObjectSocketProtocol implements SocketProtocol {

    private transient ObjectSocketEncoder encoder;

    private transient ObjectSocketDecoder decoder;

    public void initial() {
        if (encoder == null) {
            encoder = new ObjectSocketEncoder();
        }
        if (decoder == null) {
            decoder = new ObjectSocketDecoder();
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
