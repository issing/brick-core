package net.isger.brick.bus.protocol;

import java.io.IOException;

import net.isger.brick.bus.protocol.SocketProtocol.Decoder;
import net.isger.brick.core.Command;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;

public class CommandSocketDecoder implements Decoder {

    private static final DatumReader<Command> READER;

    static {
        READER = new SpecificDatumReader<Command>(Command.class);
    }

    public Object decode(byte[] data) {
        try {
            return READER.read(null,
                    DecoderFactory.get().binaryDecoder(data, null));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
