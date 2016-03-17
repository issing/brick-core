package net.isger.brick.core;

import net.isger.brick.Constants;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 预处理器
 * 
 * @author issing
 * 
 */
public class Preparer {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    /**
     * 处理
     * 
     * @param command
     */
    public final void prepare(Command command) {
        BaseCommand cmd = BaseCommand.cast(command);
        Context context = Context.getAction();
        if (context == null) {
            context = createContext(cmd);
        }
        Context.setAction(new InternalContext(console, context, cmd));
    }

    /**
     * 创建上下文
     * 
     * @param command
     * @return
     */
    protected Context createContext(final BaseCommand command) {
        return new Context() {
            public Console getConsole() {
                return console;
            }

            public BaseCommand getCommand() {
                return command;
            }
        };
    }

    /**
     * 清除
     */
    public final void cleanup() {
        Context context = Context.getAction();
        isInternal: {
            if (context instanceof InternalContext) {
                context = ((InternalContext) context).getContext();
                if (context instanceof InternalContext) {
                    break isInternal;
                }
            }
            context = null;
        }
        Context.setAction(context);
    }

}
