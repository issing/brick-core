package net.isger.brick.test;

import java.nio.ByteBuffer;
import java.util.HashMap;

import net.isger.brick.core.Command;

public class TestCommand extends Command {

    public TestCommand() {
        setHeaders(new HashMap<CharSequence, ByteBuffer>());
        setParameters(new HashMap<CharSequence, ByteBuffer>());
        setFooters(new HashMap<CharSequence, ByteBuffer>());
    }

}
