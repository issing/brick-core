package net.isger.brick.bus.protocol;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import net.isger.brick.bus.protocol.SocketProtocol.Decoder;

public class ObjectSocketDecoder implements Decoder {

    public Object decode(byte[] data) {
        try {
            ObjectInputStream oos = new ObjectInputStream(
                    new ByteArrayInputStream(data));
            return oos.readObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
