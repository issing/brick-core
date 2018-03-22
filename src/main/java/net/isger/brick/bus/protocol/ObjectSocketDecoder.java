package net.isger.brick.bus.protocol;

import java.io.InputStream;
import java.io.ObjectInputStream;

import net.isger.brick.bus.protocol.SocketProtocol.Decoder;

public class ObjectSocketDecoder implements Decoder {

    public Object decode(InputStream is) {
        try {
            return new ObjectInputStream(is).readObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
