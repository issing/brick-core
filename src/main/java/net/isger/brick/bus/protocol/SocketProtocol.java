package net.isger.brick.bus.protocol;

import java.io.InputStream;

public interface SocketProtocol extends Protocol {

    public Encoder getEncoder();

    public Decoder getDecoder();

    public interface Encoder {

        public byte[] encode(Object message);

    }

    public interface Decoder {

        public Object decode(InputStream is);

    }

}
