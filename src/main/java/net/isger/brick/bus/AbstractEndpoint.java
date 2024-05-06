package net.isger.brick.bus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.isger.brick.Constants;
import net.isger.brick.bus.protocol.Protocol;
import net.isger.brick.core.Console;
import net.isger.brick.inject.Container;
import net.isger.brick.util.CommandOperator;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 抽象端点
 * 
 * @author issing
 *
 */
@Ignore
public abstract class AbstractEndpoint implements Endpoint {

    private volatile transient Lock locker;

    @Alias(Constants.BRICK_ENCODING)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected String encoding;

    /** 控制台 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Console console;

    /** 容器 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Container container;

    /** 总线 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Bus bus;

    /** 操作器 */
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    private CommandOperator operator;

    @Ignore(mode = Mode.INCLUDE)
    private String name;

    @Ignore(mode = Mode.INCLUDE)
    private String protocol;

    private transient Protocol endpointProtocol;

    @Ignore(mode = Mode.INCLUDE, serialize = false)
    private IdentityHandler handler;

    @Ignore(mode = Mode.INCLUDE)
    private Map<String, Object> parameters;

    private transient volatile Status status;

    public AbstractEndpoint() {
        this.locker = new ReentrantLock();
        this.operator = new CommandOperator(this);
        this.parameters = new HashMap<String, Object>();
        this.status = Status.UNINITIALIZED;
    }

    public boolean hasReady() {
        return this.status == Status.INITIALIZED;
    }

    public Status getStatus() {
        return this.status;
    }

    public final void initial() {
        this.locker.lock();
        try {
            if (!(status == Status.UNINITIALIZED || status == Status.DESTROYED)) return;
            this.status = Status.INITIALIZING;
            if (Strings.isEmpty(this.protocol)) this.protocol = this.name();
            this.endpointProtocol = this.findProtocol(this.protocol, this.getClass(), null);
            if (this.handler != null) this.container.inject(this.handler);
            else this.handler = new IdentityHandlerAdapter();
            /* 等待控制台就绪 */
            this.status = Status.PENDING;
            while (!this.console.hasReady()) Helpers.sleep(200l);
        } finally {
            this.locker.unlock();
        }
        this.open();
    }

    protected synchronized boolean toActive() {
        if (this.status == Status.PENDING) {
            this.status = Status.INITIALIZED; // 已初始状态
        }
        return this.hasReady();
    }

    @SuppressWarnings("unchecked")
    private Protocol findProtocol(String name, Class<?> rawClass, String namespace) {
        Protocol protocol;
        if (Strings.isEmpty(namespace)) {
            protocol = this.bus.getProtocol(name);
        } else {
            protocol = this.bus.getProtocol(name + "." + namespace);
        }
        if (protocol == null && rawClass != AbstractEndpoint.class) {
            protocol = this.findProtocol(name, rawClass.getSuperclass(), Endpoints.getName((Class<Endpoint>) rawClass));
        }
        return protocol;
    }

    public String name() {
        if (Strings.isEmpty(this.name)) {
            this.name = Endpoints.getName(getClass());
        }
        return this.name;
    }

    public String getProtocolName() {
        return this.protocol;
    }

    public Protocol getProtocol() {
        return this.endpointProtocol;
    }

    public IdentityHandler getHandler() {
        return this.handler;
    }

    public final Object getParameter(String name) {
        return this.parameters.get(name);
    }

    public final Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(this.parameters);
    }

    public void operate(BusCommand cmd) {
        this.operator.operate(cmd);
    }

    /**
     * 打开
     */
    protected abstract void open();

    /**
     * 关闭
     */
    protected abstract void close();

    public final void destroy() {
        this.locker.lock();
        try {
            if (this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED) return;
            this.close();
            this.status = Status.DESTROYED;
        } finally {
            this.locker.unlock();
        }
    }

}
