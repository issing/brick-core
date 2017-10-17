package net.isger.brick.task;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.util.Callable;

/**
 * 任务命令
 * 
 * @author issing
 */
public class TaskCommand extends BaseCommand {

    public static final String OPERATE_SUBMIT = "submit";

    public static final String KEY_COMMAND = "task-command";

    public static final String KEY_CALLBACK = "task-callback";

    public TaskCommand() {
    }

    public TaskCommand(Command source) {
        super(source);
    }

    public TaskCommand(boolean hasShell) {
        super(hasShell);
    }

    public static TaskCommand getAction() {
        return cast(BaseCommand.getAction());
    }

    public static TaskCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == TaskCommand.class
                ? (TaskCommand) cmd : cmd.infect(new TaskCommand(false));
    }

    public Command getCommand() {
        return getHeader(KEY_COMMAND);
    }

    public void setCommand(Command command) {
        setHeader(KEY_COMMAND, command);
    }

    public <T> Callable<T> getCallback() {
        return getHeader(KEY_CALLBACK);
    }

    public void setCallback(Callable<?> callback) {
        setHeader(KEY_CALLBACK, callback);
    }

}
