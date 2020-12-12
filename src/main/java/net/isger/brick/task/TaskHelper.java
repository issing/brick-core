package net.isger.brick.task;

import net.isger.brick.core.Command;
import net.isger.brick.core.CoreHelper;
import net.isger.util.Callable;

/**
 * 任务助手
 * 
 * @author issing
 */
public class TaskHelper extends CoreHelper {

    protected TaskHelper() {
    }

    public static TaskCommand toCommand(Command task) {
        TaskCommand cmd = TaskCommand.newAction();
        if (cmd == null) {
            cmd = new TaskCommand();
        }
        cmd.setCommand(task);
        return cmd;
    }

    public static TaskCommand toCommand(boolean daemon, Command task) {
        TaskCommand cmd = toCommand(task);
        cmd.setDaemon(daemon);
        return cmd;
    }

    public static TaskCommand toCommand(boolean daemon, Command task, Callable<?> callback) {
        TaskCommand cmd = toCommand(daemon, task);
        cmd.setCallback(callback);
        return cmd;
    }

    public static TaskCommand toSubmit(Command task) {
        TaskCommand cmd = toCommand(task);
        cmd.setOperate(TaskCommand.OPERATE_SUBMIT);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static TaskCommand toSubmit(boolean daemon, Command task) {
        TaskCommand cmd = toCommand(daemon, task);
        cmd.setOperate(TaskCommand.OPERATE_SUBMIT);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static TaskCommand toSubmit(boolean daemon, Command task, Callable<?> callback) {
        TaskCommand cmd = toCommand(daemon, task, callback);
        cmd.setOperate(TaskCommand.OPERATE_SUBMIT);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

}
