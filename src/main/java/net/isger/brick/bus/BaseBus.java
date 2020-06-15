package net.isger.brick.bus;

import net.isger.brick.Constants;
import net.isger.brick.bus.protocol.Protocol;
import net.isger.brick.bus.protocol.Protocols;
import net.isger.brick.core.Console;
import net.isger.brick.inject.Container;
import net.isger.brick.task.TaskCommand;
import net.isger.util.Callable;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 总线基类
 * 
 * @author issing
 *
 */
public class BaseBus implements Bus {

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Container container;

    private Protocols protocols;

    private Endpoints endpoints;

    public BaseBus() {
        protocols = new Protocols();
        endpoints = new Endpoints();
    }

    /**
     * 总线初始
     */
    public void initial() {
        /* 初始协议 */
        for (Protocol protocol : protocols.gets().values()) {
            container.inject(protocol);
            protocol.initial();
        }
        /* 初始端点 */
        TaskCommand cmd = new TaskCommand();
        cmd.setDaemon(true);
        cmd.setOperate(TaskCommand.OPERATE_SUBMIT);
        for (final Endpoint endpoint : endpoints.gets().values()) {
            container.inject(endpoint);
            cmd.setCallback(new Callable<Exception>() {
                public Exception call(Object... args) {
                    try {
                        endpoint.initial();
                    } catch (Exception e) {
                        return e;
                    }
                    return null;
                }
            });
            console.execute(cmd);
        }
    }

    public Protocol getProtocol(String name) {
        return protocols.get(name);
    }

    public Endpoint getEndpoint(String name) {
        return endpoints.get(name);
    }

    public void destroy() {
        for (Protocol protocol : protocols.gets().values()) {
            try {
                protocol.destroy();
            } catch (Exception e) {
            }
        }
        for (Endpoint endpoint : endpoints.gets().values()) {
            try {
                endpoint.destroy();
            } catch (Exception e) {
            }
        }
    }

}
