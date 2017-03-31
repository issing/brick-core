package net.isger.brick.bus.protocol;

import net.isger.util.anno.Alias;

@Alias("command.socket")
public class CommandSocketProtocol implements SocketProtocol {

    private CommandSocketEncoder encoder;

    private CommandSocketDecoder decoder;

    public void initial() {
        if (encoder == null) {
            encoder = new CommandSocketEncoder();
        }
        if (decoder == null) {
            decoder = new CommandSocketDecoder();
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
