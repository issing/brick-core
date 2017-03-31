package net.isger.brick.bus;

/**
 * 超文本传输端点
 * 
 * @author issing
 *
 */
public abstract class HttpEndpoint extends SocketEndpoint {

    private static final int DEFAULT_PORT = 80;

    public HttpEndpoint() {
        super(null, DEFAULT_PORT);
    }

}
