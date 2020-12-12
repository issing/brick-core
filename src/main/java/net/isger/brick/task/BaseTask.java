package net.isger.brick.task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.isger.brick.Constants;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Console;
import net.isger.brick.core.Context;
import net.isger.brick.util.CommandOperator;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 基础任务
 * 
 * @author issing
 */
public class BaseTask implements Task {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    /** 操作器 */
    @Ignore(mode = Mode.INCLUDE)
    private CommandOperator operator;

    private volatile ExecutorService executor;

    private volatile ExecutorService daemonor;

    public BaseTask() {
        operator = new CommandOperator(this);
        executor = Executors.newCachedThreadPool();
        daemonor = Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public void initial() {
    }

    public void operate(TaskCommand cmd) {
        operator.operate(cmd);
    }

    /**
     * 提交任务
     *
     * @param cmd
     */
    public void submit(TaskCommand cmd) {
        final BaseCommand task = BaseCommand.cast(cmd.getCommand()); // 任务命令
        final net.isger.util.Callable<Object> callback = cmd.getCallback(); // 任务回调
        final Context context = Context.getAction(); // 任务上下文
        cmd.setResult((cmd.getDaemon() ? daemonor : executor).submit(task == null ? new Callable<Object>() {
            public Object call() throws Exception {
                Context.setAction(context);
                return callback.call();
            }
        } : new Callable<Object>() {
            public Object call() throws Exception {
                console.execute(task);
                Context.setAction(context);
                return callback == null ? task.getResult() : callback.call(task);
            }
        }));
    }

    public void destroy() {
        executor.shutdownNow();
        daemonor.shutdownNow();
    }

}
