package net.isger.brick.bind;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Console;
import net.isger.brick.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyConsole extends Console {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(DummyConsole.class);
    }

    public void operate() {
        BaseCommand cmd = Context.getAction().getCommand();
        if (cmd != null) {
            LOG.info("operate(): {}", cmd.getOperate());
        }
    }
}
