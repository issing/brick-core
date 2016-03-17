package net.isger.brick.core;

import net.isger.brick.Constants;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class BaseHandler implements Handler {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    public Object handle(Object message) {
        if (message instanceof Command) {
            BaseCommand cmd = BaseCommand.cast((Command) message);
            console.execute(cmd);
            message = cmd.getResult();
        }
        return message;
    }

}
