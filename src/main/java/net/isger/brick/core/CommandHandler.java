package net.isger.brick.core;

import net.isger.brick.Constants;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 命令处理器
 * 
 * @author issing
 */
public class CommandHandler implements Handler {

    /** 控制台 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Console console;

    public int getStatus() {
        return 1;
    }

    public Object handle(Object message) {
        if (message instanceof Command) {
            BaseCommand cmd = BaseCommand.cast((Command) message);
            this.console.execute(cmd);
            message = cmd.getResult();
        }
        return message;
    }

}
