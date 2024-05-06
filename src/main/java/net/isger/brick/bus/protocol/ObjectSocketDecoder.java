package net.isger.brick.bus.protocol;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import net.isger.util.Decoder;

public class ObjectSocketDecoder implements Decoder {

    public Object decode(byte[] content) {
        return decode(new ByteArrayInputStream(content));
    }

    @SuppressWarnings("resource")
    public Object decode(InputStream is) {
        try {
            return new ObjectInputStream(is).readObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
