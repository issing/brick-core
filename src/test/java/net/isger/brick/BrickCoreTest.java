package net.isger.brick;

import java.util.concurrent.Future;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Console;
import net.isger.brick.core.ConsoleManager;
import net.isger.brick.core.GateCommand;
import net.isger.brick.task.TaskCommand;
import net.isger.brick.test.TestCommand;
import net.isger.util.Callable;

/**
 * 核心测试
 * 
 * @author issing
 *
 */
public class BrickCoreTest extends TestCase {

    public BrickCoreTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(BrickCoreTest.class);
    }

    public void testBrick() {
        // 初始环境
        ConsoleManager manager = new ConsoleManager();
        Console console = manager.getConsole();

        // 模块操作
        TaskCommand taskCmd = new TaskCommand(new TestCommand());
        taskCmd.setOperate(TaskCommand.OPERATE_SUBMIT);
        BaseCommand command = (BaseCommand) taskCmd.cast().clone();
        command.setOperate("say");
        taskCmd.setCommand(command);
        taskCmd.setCallback(new Callable<Object>() {
            public Object call(Object... args) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                return ((BaseCommand) args[0]).getResult();
            }
        });
        console.execute(taskCmd);
        Future<?> sayFuture = (Future<?>) taskCmd.getResult();
        command = (BaseCommand) command.clone();
        command.setOperate("other");
        taskCmd.setCommand(command);
        taskCmd.setCallback(new Callable<Object>() {
            public Object call(Object... args) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                return ((BaseCommand) args[0]).getResult();
            }
        });
        console.execute(taskCmd);
        Future<?> otherFuture = (Future<?>) taskCmd.getResult();

        // 应用操作
        GateCommand gateCmd = GateCommand.cast(taskCmd);
        gateCmd.setDomain("test-domain");
        gateCmd.setOperate("say");
        console.execute(gateCmd);
        gateCmd.setOperate("other");
        console.execute(gateCmd);

        try {
            System.out.println(sayFuture.get());
            System.out.println(otherFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConsoleManager manager = new ConsoleManager();
        // Console console =
        manager.getConsole();
        // BusCommand cmd = new BusCommand();
        // cmd.setEndpoint("cc");
        // cmd.setOperate("send");
        // cmd.setPayload(cmd);
        // console.execute(cmd);
    }

}
