package net.isger.brick.bus;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.isger.brick.bus.protocol.SocketProtocol;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 套接字端点
 * 
 * @author issing
 *
 */
@Ignore
public abstract class SocketEndpoint extends AbstractEndpoint {

    public static final int MIN_RETRIES = 3;

    @Ignore(mode = Mode.INCLUDE)
    private int retries;

    @Ignore(mode = Mode.INCLUDE)
    private String host;

    @Ignore(mode = Mode.INCLUDE)
    private int port;

    private InetSocketAddress address;

    public SocketEndpoint() {
        this(null, 0);
    }

    public SocketEndpoint(String host, int port) {
        if (Strings.isEmpty(host)) {
            try {
                host = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                host = "localhost";
            }
        }
        this.host = host;
        this.port = port;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public SocketProtocol getProtocol() {
        return (SocketProtocol) super.getProtocol();
    }

    protected void open() {
        address = new InetSocketAddress(host, port);
        if (retries < MIN_RETRIES) {
            retries = MIN_RETRIES;
        }
    }

    protected void close() {
    }

}
