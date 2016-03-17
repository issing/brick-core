package net.isger.brick.task;

import java.util.Map;

import net.isger.brick.Constants;
import net.isger.brick.core.AbstractModule;
import net.isger.brick.inject.ConstantStrategy;
import net.isger.util.Asserts;
import net.isger.util.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskModule extends AbstractModule {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(TaskModule.class);
    }

    /**
     * 任务目标类型
     */
    public Class<?> getTargetClass() {
        Class<?> targetClass = super.getTargetClass();
        if (targetClass == null) {
            targetClass = Task.class;
        } else {
            Asserts.argument(Task.class.isAssignableFrom(targetClass),
                    "The task " + targetClass + " must implement the "
                            + Task.class);
        }
        return targetClass;
    }

    /**
     * 任务实现类型
     */
    public Class<?> getImplementClass() {
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
        Class<?> type = getTargetClass();
        setTask(type, Constants.SYSTEM, task);
        if (type != task.getClass()) {
            setTask(task.getClass(), Constants.SYSTEM, task);
        }
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
     * 创建任务实例（暂不支持键值对以外配置方式）
     */
    protected Object create(Object res) {
        throw new IllegalArgumentException("Unexpected config " + res);
    }

    public void initial() {
        super.initial();
        /* 初始任务 */
        Task task = getTask();
        if (task == null) {
            setTask(task = new BaseTask());
            container.inject(task);
        }
        task.initial();
    }

    public void execute() {
        Callable<?> callable = TaskCommand.getAction().getCallback();
        if (callable == null) {
            /* 模块操作 */
            operate();
        } else {
            /* 任务操作 */
            Task task = getTask();
            setInternal(Task.BRICK_TASK, task);
            task.operate();
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
