package net.isger.brick.bus;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    public void initial() {
        for (Protocol protocol : protocols.get().values()) {
            container.inject(protocol);
            protocol.initial();
        }
        TaskCommand cmd = new TaskCommand();
        cmd.setOperate(TaskCommand.OPERATE_SUBMIT);
        for (final Endpoint endpoint : endpoints.get().values()) {
            container.inject(endpoint);
            cmd.setCallback(new Callable<Void>() {
                public Void call(Object... args) {
                    endpoint.initial();
                    return null;
                }
            });
            console.execute(cmd);
            ready((Future<?>) cmd.getResult(), endpoint);
        }
    }

    private void ready(Future<?> future, Endpoint endpoint) {
        do {
            try {
                future.get(100, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
            }
        } while (endpoint.getStatus() == null);
    }

    public Protocol getProtocol(String name) {
        return protocols.get(name);
    }

    public Endpoint getEndpoint(String name) {
        return endpoints.get(name);
    }

    public void destroy() {
        for (Protocol protocol : protocols.get().values()) {
            protocol.destroy();
        }
        for (Endpoint endpoint : endpoints.get().values()) {
            endpoint.destroy();
        }
    }

}
