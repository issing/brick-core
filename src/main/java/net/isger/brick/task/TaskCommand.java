package net.isger.brick.task;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.util.Callable;
import net.isger.util.Helpers;

/**
 * 任务命令
 * 
 * @author issing
 */
public class TaskCommand extends BaseCommand {

    public static final String CTRL_DAEMON = "task-daemon";

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

    public static TaskCommand newAction() {
        return cast(BaseCommand.newAction());
    }

    public static TaskCommand mockAction() {
        return cast(BaseCommand.mockAction());
    }

    public static TaskCommand realAction() {
        return cast(BaseCommand.realAction());
    }

    public static TaskCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == TaskCommand.class ? (TaskCommand) cmd : cmd.infect(new TaskCommand(false));
    }

    public boolean getDaemon() {
        return getDaemon(this);
    }

    public void setDaemon(boolean daemon) {
        setDaemon(this, daemon);
    }

    public Command getCommand() {
        return getCommand(this);
    }

    public void setCommand(Command command) {
        setCommand(this, command);
    }

    public <T> Callable<T> getCallback() {
        return getCallback(this);
    }

    public void setCallback(Callable<?> callback) {
        setCallback(this, callback);
    }

    public static boolean getDaemon(BaseCommand cmd) {
        return Helpers.toBoolean(cmd.getHeader(CTRL_DAEMON));
    }

    public static void setDaemon(BaseCommand cmd, boolean deamon) {
        cmd.setHeader(CTRL_DAEMON, deamon);
    }

    public static Command getCommand(BaseCommand cmd) {
        return cmd.getHeader(CTRL_COMMAND);
    }

    public static void setCommand(BaseCommand cmd, Command command) {
        cmd.setHeader(CTRL_COMMAND, command);
    }

    public static <T> Callable<T> getCallback(BaseCommand cmd) {
        return cmd.getHeader(CTRL_CALLBACK);
    }

    public static void setCallback(BaseCommand cmd, Callable<?> callback) {
        cmd.setHeader(CTRL_CALLBACK, callback);
    }

}
