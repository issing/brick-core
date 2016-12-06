package net.isger.brick.bus.protocol;

import java.io.InputStream;
import java.io.ObjectInputStream;

import net.isger.brick.bus.Decoder;

public class ObjectDecoder implements Decoder {

    public Object decode(InputStream is) {
        try {
            ObjectInputStream oos = new ObjectInputStream(is);
            return oos.readObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
