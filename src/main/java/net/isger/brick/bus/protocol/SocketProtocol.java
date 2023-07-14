package net.isger.brick.bus.protocol;

import net.isger.util.Decoder;
import net.isger.util.Encoder;

public interface SocketProtocol extends Protocol {

    public Encoder getEncoder();

    public Decoder getDecoder();

}
