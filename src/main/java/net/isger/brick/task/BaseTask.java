package net.isger.brick.task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.isger.brick.Constants;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Console;
import net.isger.brick.util.DynamicOperator;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class BaseTask extends DynamicOperator implements Task {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    private ExecutorService executor;

    public void initial() {
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
    }

    public void submit() {
        TaskCommand taskCmd = TaskCommand.getAction();
        final BaseCommand command = BaseCommand.cast(taskCmd.getCommand());
        final net.isger.util.Callable<?> callback = taskCmd.getCallback();
        taskCmd.setResult(executor.submit(command == null ? callback
                : new Callable<Object>() {
                    public Object call() throws Exception {
                        console.execute(command);
                        return callback == null ? command.getResult()
                                : callback.call(command);
                    }
                }));
    }

    public void destroy() {
        executor.shutdownNow();
    }

}
