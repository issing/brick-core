package net.isger.brick.bus.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.isger.brick.bus.protocol.SocketProtocol.Encoder;
import net.isger.brick.core.Command;
import net.isger.util.Files;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;

public class CommandSocketEncoder implements Encoder {

    private static final DatumWriter<Command> WRITER;

    static {
        WRITER = new SpecificDatumWriter<Command>(Command.class);
    }

    public byte[] encode(Object message) {
        ByteArrayOutputStream out;
        org.apache.avro.io.Encoder encoder = EncoderFactory.get()
                .binaryEncoder(out = new ByteArrayOutputStream(), null);
        try {
            WRITER.write((Command) message, encoder);
            encoder.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            Files.close(out);
        }
        return out.toByteArray();
    }

}
