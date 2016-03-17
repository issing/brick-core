package net.isger.brick.task;

import net.isger.util.Manageable;
import net.isger.util.Operator;

/**
 * 任务接口
 * 
 * @author issing
 *
 */
public interface Task extends Operator, Manageable {

    public static final String BRICK_TASK = "brick.core.task";

}
