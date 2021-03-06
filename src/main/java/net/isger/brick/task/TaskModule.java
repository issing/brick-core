package net.isger.brick.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.core.AbstractModule;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.inject.ConstantStrategy;
import net.isger.util.Asserts;

/**
 * 任务模块
 * 
 * @author issing
 */
public class TaskModule extends AbstractModule {

    private static final String TASK = "task";

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(TaskModule.class);
    }

    /**
     * 任务目标类型
     */
    public Class<? extends Task> getTargetClass() {
        return Task.class;
    }

    /**
     * 任务实现类型
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Task> getImplementClass() {
        return (Class<? extends Task>) getImplementClass(TASK);
    }

    /**
     * 任务基本实现
     */
    public Class<? extends Task> getBaseClass() {
        return BaseTask.class;
    }

    /**
     * 创建任务
     */
    protected Object create(Class<?> clazz, Map<String, Object> res) {
        Task task = (Task) super.create(clazz, res);
        setTask(task);
        return task;
    }

    /**
     * 获取任务
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Task getTask() {
        return container.getInstance((Class<Task>) getTargetClass(),
                Constants.SYSTEM);
    }

    /**
     * 设置任务
     * 
     * @param task
     */
    protected void setTask(Task task) {
        Asserts.isNotNull(task, "The task cannot be null");
        if (LOG.isDebugEnabled()) {
            LOG.info("Achieve task [{}]", task);
        }
        setTask(Task.class, Constants.SYSTEM, task);
    }

    /**
     * 设置任务
     * 
     * @param type
     * @param name
     * @param task
     */
    private void setTask(Class<?> type, String name, Object task) {
        task = ConstantStrategy.set(container, type, name, task);
        if (LOG.isDebugEnabled() && task != null) {
            LOG.info("(!) Discard task [{}]", task);
        }
    }

    /**
     * 创建默认任务
     */
    protected Task create() {
        return (Task) super.create();
    }

    public void initial() {
        super.initial();
        /* 初始任务 */
        Task task = getTask();
        if (task == null) {
            setTask(create());
            task = getTask();
        }
        task.initial();
    }

    public final void execute(BaseCommand cmd) {
        TaskCommand payload = (TaskCommand) cmd;
        if (payload.getCallback() == null && payload.getCommand() == null) {
            /* 模块操作 */
            super.execute(payload);
        } else {
            /* 任务操作 */
            Task task = getTask();
            setInternal(Task.BRICK_TASK, task);
            task.operate(payload);
        }
    }

    public void destroy() {
        /* 注销任务 */
        Task task = getTask();
        if (task != null) {
            task.destroy();
        }
        super.destroy();
    }

}
