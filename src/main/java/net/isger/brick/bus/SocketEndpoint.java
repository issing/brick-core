package net.isger.brick.bus;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public abstract class SocketEndpoint extends AbstractEndpoint {

    public static final int MIN_RETRIES = 3;

    private static final int DEFAULT_PORT = 1109;

    @Ignore(mode = Mode.INCLUDE)
    private int retries;

    @Ignore(mode = Mode.INCLUDE)
    private String host;

    @Ignore(mode = Mode.INCLUDE)
    private Integer port;

    private SocketAddress address;

    public SocketEndpoint() {
        retries = MIN_RETRIES;
        port = DEFAULT_PORT;
        try {
            host = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
        }
    }

    public SocketAddress getAddress() {
        return address;
    }

    protected void open() {
        address = new InetSocketAddress(host, port);
    }

}
