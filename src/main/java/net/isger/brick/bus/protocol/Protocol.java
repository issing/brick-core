package net.isger.brick.bus.protocol;

import net.isger.brick.bus.Decoder;
import net.isger.brick.bus.Encoder;
import net.isger.util.Manageable;

public interface Protocol extends Manageable {

    public Encoder getEncoder();

    public Decoder getDecoder();

}
