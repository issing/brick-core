package net.isger.brick.bus;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.isger.brick.Constants;
import net.isger.brick.core.BaseHandler;
import net.isger.brick.core.Handler;
import net.isger.brick.inject.Container;
import net.isger.brick.util.DynamicOperator;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public abstract class SocketEndpoint extends DynamicOperator implements
        Endpoint {

    public static final int MIN_RETRIES = 3;

    private static final String DEFAULT_PROTOCOL = "object";

    private static final int DEFAULT_PORT = 1109;

    /** 容器 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Container container;

    protected String protocol;

    private String host;

    private Integer port;

    protected transient InetSocketAddress address;

    protected Handler handler;

    protected int retries = MIN_RETRIES;

    public final void initial() {
        if (Strings.isEmpty(protocol)) {
            protocol = DEFAULT_PROTOCOL;
        }
        if (Strings.isEmpty(host)) {
            try {
                host = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        if (port == null) {
            port = DEFAULT_PORT;
        }
        address = new InetSocketAddress(host, port);
        if (handler == null) {
            handler = new BaseHandler();
        }
        container.inject(handler);
        open();
    }

    /**
     * 打开
     * 
     * @param address
     */
    protected abstract void open();

    /**
     * 关闭
     */
    protected abstract void close();

    public final void destroy() {
        close();
    }

}
