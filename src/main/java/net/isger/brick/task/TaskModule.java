package net.isger.brick.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.core.AbstractModule;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.inject.ConstantStrategy;
import net.isger.util.Asserts;
import net.isger.util.reflect.ClassAssembler;

/**
 * 任务模块
 * 
 * @author issing
 */
public class TaskModule extends AbstractModule {

    private static final String TASK = "task";

    private static final Logger LOG;

    private transient volatile Status status;

    static {
        LOG = LoggerFactory.getLogger(TaskModule.class);
    }

    public TaskModule() {
        this.status = Status.UNINITIALIZED;
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
        return (Class<? extends Task>) this.getImplementClass(TASK);
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
    protected Object create(Class<?> clazz, Map<String, Object> res, ClassAssembler assembler) {
        Task task = (Task) super.create(clazz, res, assembler);
        this.setTask(task);
        return task;
    }

    /**
     * 获取任务
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Task getTask() {
        return this.container.getInstance((Class<Task>) this.getTargetClass(), Constants.SYSTEM);
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
        this.setTask(Task.class, Constants.SYSTEM, task);
    }

    /**
     * 设置任务
     * 
     * @param type
     * @param name
     * @param task
     */
    private void setTask(Class<?> type, String name, Object task) {
        task = ConstantStrategy.set(this.container, type, name, task);
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

    public boolean hasReady() {
        return this.getTask().hasReady() && this.status == Status.INITIALIZED;
    }

    public Status getStatus() {
        return this.status;
    }

    public synchronized void initial() {
        if (!(status == Status.UNINITIALIZED || status == Status.DESTROYED)) return;
        this.status = Status.INITIALIZING;
        super.initial();
        /* 初始任务 */
        Task task = this.getTask();
        if (task == null) {
            this.setTask(create());
            task = this.getTask();
        }
        task.initial();
        this.status = Status.INITIALIZED;
    }

    public final void execute(BaseCommand cmd) {
        TaskCommand payload = (TaskCommand) cmd;
        if (payload.getCallback() == null && payload.getCommand() == null) {
            /* 模块操作 */
            super.execute(payload);
        } else {
            /* 任务操作 */
            Task task = this.getTask();
            this.setInternal(Task.BRICK_TASK, task);
            task.operate(payload);
        }
    }

    public synchronized void destroy() {
        if (this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED) return;
        /* 注销任务 */
        Task task = this.getTask();
        if (task != null) {
            task.destroy();
        }
        super.destroy();
        this.status = Status.DESTROYED;
    }

}
