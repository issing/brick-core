package net.isger.brick.bus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.isger.brick.Constants;
import net.isger.brick.bus.protocol.Protocol;
import net.isger.brick.core.CommandHandler;
import net.isger.brick.core.Handler;
import net.isger.brick.inject.Container;
import net.isger.brick.util.CommandOperator;
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

    private static final String DEFAULT_PROTOCOL = "object";

    /** 容器 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Container container;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Bus bus;

    /** 操作器 */
    @Ignore(mode = Mode.INCLUDE)
    private CommandOperator operator;

    @Ignore(mode = Mode.INCLUDE)
    private String name;

    private Status status;

    @Ignore(mode = Mode.INCLUDE)
    private String protocol;

    private Protocol endpointProtocol;

    @Ignore(mode = Mode.INCLUDE)
    private Handler handler;

    @Ignore(mode = Mode.INCLUDE)
    private Map<String, Object> parameters;

    public AbstractEndpoint() {
        operator = new CommandOperator(this);
        status = Status.INACTIVATE;
        parameters = new HashMap<String, Object>();
    }

    public final void initial() {
        if (Strings.isEmpty(protocol)) {
            protocol = DEFAULT_PROTOCOL;
        }
        endpointProtocol = bus.getProtocol(this.protocol);
        if (protocol == null) {
            endpointProtocol = bus.getProtocol(DEFAULT_PROTOCOL);
        }
        if (handler == null) {
            handler = new CommandHandler();
        }
        container.inject(handler);
        open();
        status = Status.ACTIVATED;
    }

    public String name() {
        if (Strings.isEmpty(name)) {
            name = Endpoints.getName(this.getClass());
        }
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public String getProtocolName() {
        return protocol;
    }

    public Protocol getProtocol() {
        return endpointProtocol;
    }

    public Handler getHandler() {
        return handler;
    }

    protected final Object getParameter(String name) {
        return parameters.get(name);
    }

    protected final Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void operate(BusCommand cmd) {
        operator.operate(cmd);
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
        if (status != Status.DEACTIVATED) {
            close();
        }
        status = Status.DEACTIVATED;
    }

}
