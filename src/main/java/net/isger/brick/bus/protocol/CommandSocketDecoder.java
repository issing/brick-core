package net.isger.brick.bus.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;

import net.isger.brick.core.Command;
import net.isger.util.Decoder;

public class CommandSocketDecoder implements Decoder {

    private static final DatumReader<Command> READER;

    static {
        READER = new SpecificDatumReader<Command>(Command.class);
    }

    public Object decode(byte[] content) {
        return decode(new ByteArrayInputStream(content));
    }

    public Object decode(InputStream is) {
        try {
            return READER.read(null, DecoderFactory.get().binaryDecoder(is, null));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
