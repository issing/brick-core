package net.isger.brick.bus.protocol;

import net.isger.brick.bus.Decoder;
import net.isger.brick.bus.Encoder;

public class CommandProtocol implements Protocol {

    private CommandEncoder encoder;

    private CommandDecoder decoder;

    public void initial() {
        if (encoder == null) {
            encoder = new CommandEncoder();
        }
        if (decoder == null) {
            decoder = new CommandDecoder();
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
