package net.isger.brick.bus.protocol;

import net.isger.brick.bus.Decoder;
import net.isger.brick.bus.Encoder;

public class ObjectProtocol implements Protocol {

    private transient ObjectEncoder encoder;

    private transient ObjectDecoder decoder;

    public void initial() {
        if (encoder == null) {
            encoder = new ObjectEncoder();
        }
        if (decoder == null) {
            decoder = new ObjectDecoder();
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
