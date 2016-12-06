package net.isger.brick.task;

import net.isger.util.Manageable;

/**
 * 任务接口
 * 
 * @author issing
 *
 */
public interface Task extends Manageable {

    public static final String BRICK_TASK = "brick.core.task";

    public void operate(TaskCommand tcmd);

}
