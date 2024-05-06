package net.isger.brick.bus;

import net.isger.brick.Constants;
import net.isger.brick.bus.protocol.Protocol;
import net.isger.brick.bus.protocol.Protocols;
import net.isger.brick.core.Console;
import net.isger.brick.inject.Container;
import net.isger.brick.task.TaskCommand;
import net.isger.util.Callable;
import net.isger.util.Helpers;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 总线基类
 * 
 * @author issing
 */
public class BaseBus implements Bus {

    private transient volatile Status status;

    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Console console;

    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Container container;

    private Protocols protocols;

    private Endpoints endpoints;

    public BaseBus() {
        this.protocols = new Protocols();
        this.endpoints = new Endpoints();
        this.status = Status.UNINITIALIZED;
    }

    public boolean hasReady() {
        for (Endpoint endpoint : this.endpoints.gets().values()) {
            if (!endpoint.hasReady()) {
                return false;
            }
        }
        return this.status == Status.INITIALIZED;
    }

    public Status getStatus() {
        return this.status;
    }

    /**
     * 总线初始
     */
    public synchronized void initial() {
        if (!(status == Status.UNINITIALIZED || status == Status.DESTROYED)) return;
        this.status = Status.INITIALIZING;
        /* 初始协议 */
        for (Protocol protocol : this.protocols.gets().values()) {
            this.container.inject(protocol); // 依赖注入
            protocol.initial();
        }
        /* 初始端点（线程任务） */
        TaskCommand cmd = new TaskCommand();
        cmd.setDaemon(true); // 后台守护进程方式
        cmd.setOperate(TaskCommand.OPERATE_SUBMIT);
        for (final Endpoint endpoint : this.endpoints.gets().values()) {
            this.container.inject(endpoint); // 依赖注入
            cmd.setCallback(new Callable<Exception>() {
                public Exception call(Object... args) {
                    Exception cause = null;
                    try {
                        endpoint.initial();
                    } catch (Exception e) {
                        cause = e;
                    }
                    return cause;
                }
            });
            this.console.execute(cmd.clone()); // 执行初始任务
            while (endpoint.getStatus() != null && endpoint.getStatus().value < Status.PENDING.value) Helpers.sleep(200l); // 等待就绪
        }
        this.status = Status.INITIALIZED;
    }

    /**
     * 获取协议
     */
    public Protocol getProtocol(String name) {
        return this.protocols.get(name);
    }

    /**
     * 获取端点
     */
    public Endpoint getEndpoint(String name) {
        return this.endpoints.get(name);
    }

    /**
     * 总线注销
     */
    public synchronized void destroy() {
        if (this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED) return;
        /* 注销端点 */
        for (Endpoint endpoint : this.endpoints.gets().values()) {
            try {
                endpoint.destroy();
            } catch (Exception e) {
            }
        }
        /* 注销协议 */
        for (Protocol protocol : this.protocols.gets().values()) {
            try {
                protocol.destroy();
            } catch (Exception e) {
            }
        }
        this.status = Status.DESTROYED;
    }

}
