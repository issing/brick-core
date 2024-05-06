package net.isger.brick.core;

import net.isger.brick.Constants;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 制备器
 * 
 * @author issing
 * 
 */
public class Preparer {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    @Alias(Constants.SYSTEM)
    protected Console console;

    /**
     * 配备
     * 
     * @param command
     */
    public final void prepare(Command command) {
        BaseCommand cmd = BaseCommand.cast(command);
        Context context = Context.getAction();
        if (context == null) {
            context = new InternalContext(createContext(cmd));
        } else {
            updateContext(context = new InternalContext(context), cmd);
        }
        Context.setAction(context);
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
     * 更新上下文
     * 
     * @param context
     * @param cmd
     */
    protected void updateContext(Context context, BaseCommand cmd) {
        ((InternalContext) context).command = cmd;
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
                    break isInternal; // 恢复上下文
                }
            }
            context = null; // 清空上下文
        }
        Context.setAction(context);
    }

}
