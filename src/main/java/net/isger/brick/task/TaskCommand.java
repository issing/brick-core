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

    public static final String CTRL_COMMAND = "task-command";

    public static final String CTRL_CALLBACK = "task-callback";

    public static final String OPERATE_SUBMIT = "submit";

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
                ? (TaskCommand) cmd
                : cmd.infect(new TaskCommand(false));
    }

    public Command getCommand() {
        return getHeader(CTRL_COMMAND);
    }

    public void setCommand(Command command) {
        setHeader(CTRL_COMMAND, command);
    }

    public <T> Callable<T> getCallback() {
        return getHeader(CTRL_CALLBACK);
    }

    public void setCallback(Callable<?> callback) {
        setHeader(CTRL_CALLBACK, callback);
    }

}
